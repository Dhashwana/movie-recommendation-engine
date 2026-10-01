package com.movie.recommendation.service;

import com.movie.recommendation.model.Movie;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class MovieService {
    private final TmdbService tmdbService;

    public MovieService(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    public List<Movie> recommend(String genre, double minRating, int minYear) {
        List<Movie> movies = tmdbService.getMovies(genre, minRating);

        Predicate<Movie> genrePredicate = movie ->
                genre.equalsIgnoreCase("All") ||
                        movie.getGenre().toLowerCase().contains(genre.toLowerCase());

        Predicate<Movie> ratingPredicate = movie ->
                movie.getRating() >= minRating;

        Predicate<Movie> yearPredicate = movie ->
                movie.getYear() >= minYear;

        Predicate<Movie> recommendationPredicate =
                genrePredicate
                        .and(ratingPredicate)
                        .and(yearPredicate);

        return movies.stream()
                .filter(recommendationPredicate)
                .sorted(Comparator.comparing(Movie::getRating).reversed())
                .limit(12)
                .collect(Collectors.toList());
    }

    public Optional<Movie> findMovieById(int id) {
        Movie movie = tmdbService.getMovieById(id);
        return Optional.ofNullable(movie);
    }
}