package ru.yandex.practicum.filmorate.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;


@RestController
@Validated
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable @Positive Long id) {
        return filmService.getFilmById(id);
    }


    @PostMapping
    public Film addFilm(@RequestBody @Valid Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film changeFilm(@RequestBody Film film) {
        return filmService.changeFilm(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public Film addLike(@PathVariable @Positive Long id, @PathVariable @Positive Long userId) {
        return filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Film deleteLike(@PathVariable @Positive Long id, @PathVariable @Positive Long userId) {
        return filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Integer year) {
        return filmService.getPopularFilms(count, genreId, year);
    }
}
