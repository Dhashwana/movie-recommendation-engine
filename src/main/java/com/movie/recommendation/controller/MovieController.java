package com.movie.recommendation.controller;

import com.movie.recommendation.model.Movie;
import com.movie.recommendation.service.MovieService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/movies")
@CrossOrigin
public class MovieController {
    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/recommend")
    public List<Movie> recommend(
            @RequestParam String genre,
            @RequestParam double rating,
            @RequestParam int year) {
        return movieService.recommend(
                genre,
                rating,
                year
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Movie> getMovieById(@PathVariable int id) {
        Optional<Movie> movie = movieService.findMovieById(id);

        return movie.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}