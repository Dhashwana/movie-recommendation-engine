package com.movie.recommendation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movie.recommendation.model.Movie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TmdbService {
    @Value("${tmdb.api.token}")
    private String apiToken;

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<Integer, String> genreNames = Map.ofEntries(
            Map.entry(28, "Action"),
            Map.entry(12, "Adventure"),
            Map.entry(16, "Animation"),
            Map.entry(35, "Comedy"),
            Map.entry(80, "Crime"),
            Map.entry(99, "Documentary"),
            Map.entry(18, "Drama"),
            Map.entry(10751, "Family"),
            Map.entry(14, "Fantasy"),
            Map.entry(27, "Horror"),
            Map.entry(10402, "Music"),
            Map.entry(9648, "Mystery"),
            Map.entry(10749, "Romance"),
            Map.entry(878, "Sci-Fi"),
            Map.entry(53, "Thriller"),
            Map.entry(10752, "War")
    );

    public List<Movie> getMovies(String genre, double minRating) {
        try {
            StringBuilder url = new StringBuilder();
            url.append(baseUrl);
            url.append("/discover/movie?");
            url.append("language=en-US");
            url.append("&sort_by=vote_average.desc");
            url.append("&vote_average.gte=").append(minRating);
            url.append("&vote_count.gte=100");

            if (!genre.equalsIgnoreCase("All")) {
                Integer genreId = getGenreId(genre);

                if (genreId != null) {
                    url.append("&with_genres=").append(genreId);
                }
            }

            url.append("&page=1");

            String responseBody = sendRequest(url.toString());

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode results = root.get("results");

            List<Movie> movies = new ArrayList<>();

            for (JsonNode movieNode : results) {
                int id = movieNode.get("id").asInt();
                String title = movieNode.get("title").asText();
                double rating = movieNode.get("vote_average").asDouble();

                String releaseDate = movieNode.get("release_date").asText();
                int year = releaseDate.isEmpty()
                        ? 0
                        : Integer.parseInt(releaseDate.substring(0, 4));

                String posterPath = movieNode.get("poster_path").isNull()
                        ? ""
                        : movieNode.get("poster_path").asText();

                String poster = posterPath.isEmpty()
                        ? ""
                        : "https://image.tmdb.org/t/p/w500" + posterPath;

                String movieGenre = getGenreName(movieNode.get("genre_ids"));
                String overview = movieNode.get("overview").asText();

                movies.add(new Movie(
                        id,
                        title,
                        movieGenre,
                        rating,
                        year,
                        poster,
                        overview
                ));
            }

            return movies;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Unable to connect to TMDB", e);
        }
    }

    public Movie getMovieById(int id) {
        try {
            String url = baseUrl + "/movie/" + id + "?language=en-US";

            String responseBody = sendRequest(url);

            JsonNode movieNode = objectMapper.readTree(responseBody);

            int movieId = movieNode.get("id").asInt();
            String title = movieNode.get("title").asText();
            double rating = movieNode.get("vote_average").asDouble();

            String releaseDate = movieNode.get("release_date").asText();
            int year = releaseDate.isEmpty()
                    ? 0
                    : Integer.parseInt(releaseDate.substring(0, 4));

            String posterPath = movieNode.get("poster_path").isNull()
                    ? ""
                    : movieNode.get("poster_path").asText();

            String poster = posterPath.isEmpty()
                    ? ""
                    : "https://image.tmdb.org/t/p/w500" + posterPath;

            String overview = movieNode.get("overview").asText();
            String movieGenre = getDetailedGenreName(movieNode.get("genres"));

            return new Movie(
                    movieId,
                    title,
                    movieGenre,
                    rating,
                    year,
                    poster,
                    overview
            );
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Unable to connect to TMDB", e);
        }
    }

    private String sendRequest(String url) throws IOException, InterruptedException {
        IOException lastException = null;

        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(20))
                        .header("Authorization", "Bearer " + apiToken)
                        .header("accept", "application/json")
                        .header("User-Agent", "cinefind/1.0")
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() != 200) {
                    throw new RuntimeException(
                            "TMDB request failed: " + response.statusCode()
                    );
                }

                return response.body();
            } catch (IOException e) {
                lastException = e;

                if (attempt < 3) {
                    Thread.sleep(1000);
                }
            }
        }

        throw lastException;
    }

    private Integer getGenreId(String genre) {
        return switch (genre.toLowerCase()) {
            case "action" -> 28;
            case "sci-fi" -> 878;
            case "comedy" -> 35;
            case "romance" -> 10749;
            case "horror" -> 27;
            default -> null;
        };
    }

    private String getGenreName(JsonNode genreIds) {
        List<String> names = new ArrayList<>();

        for (JsonNode genreId : genreIds) {
            String name = genreNames.get(genreId.asInt());

            if (name != null) {
                names.add(name);
            }
        }

        return names.isEmpty()
                ? "Unknown"
                : String.join(", ", names);
    }

    private String getDetailedGenreName(JsonNode genres) {
        List<String> names = new ArrayList<>();

        for (JsonNode genre : genres) {
            String name = genre.get("name").asText();
            names.add(name);
        }

        return names.isEmpty()
                ? "Unknown"
                : String.join(", ", names);
    }
}