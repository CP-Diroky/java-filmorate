package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;


@Data
public class Film {
    private Long id;
    @NotBlank
    private String name;
    @Size(min = 1, max = 200)
    private String description;
    @NotNull
    private LocalDate releaseDate;
    @Min(1)
    private Integer duration;
    private Set<Long> usersId = new HashSet<>(); //Множество id пользователей которые поставили лайк фильму
    private Set<Genre> genres = new LinkedHashSet<>(); //Множество жанров
    private Set<Director> directors = new HashSet<>(); //Множество режиссёров
    private Mpa mpa; //рейтинг фильма
}
