package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Review {
    private Long reviewId;
    @NotBlank
    private String content;
    @NotNull
    @JsonProperty("isPositive")
    private Boolean positive;
    @NotNull
    private Long userId;
    @NotNull
    private Long filmId;
    private Long useful;
}
