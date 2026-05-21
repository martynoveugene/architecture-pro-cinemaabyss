package com.cinemaabyss.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class MovieEvent implements Identifiable {

    @NotNull(message = "movie_id is required")
    @JsonProperty("movie_id")
    private Long movieId;

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "action is required")
    private String action;

    @JsonProperty("user_id")
    private Long userId;

    private Float rating;

    private String genres;

    private String items;

    private String description;

    @Override
    public String getId() {
        return "movie-"+this.movieId+"-"+this.action;
    }
}
