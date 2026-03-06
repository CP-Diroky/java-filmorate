package ru.yandex.practicum.filmorate.storage;


import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private Map<Long, Film> films = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(InMemoryFilmStorage.class);


    public long getNextId() {
        long id = films.values().stream()
                .mapToLong(Film::getId)
                .max().orElse(0);
        id++;
        return id;
    }

    @Override
    public Collection<Film> getAllFilms() {
        log.info("Список фильмов получен");
        return films.values();
    }

    @Override
    public Film addFilm(@Valid Film film) {
        String errorMessage;
        if (film == null) {
            errorMessage = "Неверный ввод!";
            log.error(errorMessage);
            throw new ConditionsNotMetException(errorMessage);
        } else if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            errorMessage = "Дата выпуска не должна быть раньше 28 декабря 1895 года!";
            log.error(errorMessage);
            throw new ConditionsNotMetException(errorMessage);
        }
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм добавлен");
        return film;
    }

    @Override
    public Film changeFilm(Film film) {
        String errorMessage;
        if (film == null || !films.containsKey(film.getId())) {
            errorMessage = "Фильм не найден!";
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        Film newFilm = films.get(film.getId());
        if (film.getName() != null && !film.getName().isBlank()) {
            newFilm.setName(film.getName());
        }
        if (film.getDescription() != null && !film.getDescription().isBlank()) {
            newFilm.setDescription(film.getDescription());
        }
        if (film.getReleaseDate() != null
                && !film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            newFilm.setReleaseDate(film.getReleaseDate());
        }
        if (film.getDuration() != null && film.getDuration() > 0) {
            newFilm.setDuration(film.getDuration());
        }
        films.put(newFilm.getId(), newFilm);
        log.info("Данные фильма обновлены!");
        return newFilm;
    }

    @Override
    public Film getFilmById(Long id) {
        if (id == null || !films.containsKey(id)) {
            throw new NotFoundException("Фильм не найден!");
        }
        return films.get(id);
    }

}
