package com.movie.recommendation.model;

public class Movie {
    private int id;
    private String title;
    private String genre;
    private double rating;
    private int year;
    private String poster;
    private String overview;

    public Movie(int id, String title, String genre, double rating, int year, String poster, String overview) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.rating = rating;
        this.year = year;
        this.poster = poster;
        this.overview = overview;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public double getRating() {
        return rating;
    }

    public int getYear() {
        return year;
    }

    public String getPoster() {
        return poster;
    }

    public String getOverview() {
        return overview;
    }
}