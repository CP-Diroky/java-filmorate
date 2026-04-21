package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {

    Collection<Film> getAllFilms();

    Film addFilm(Film film);

    Film changeFilm(Film film);

    Film getFilmById(Long id);

    Film addLike(Long filmId, Long userId);

    Film deleteLike(Long filmId, Long userId);

    List<Film> getPopularFilms(int count, Long genreId, Integer year);

    List<Film> getCommonFilms(Long userId, Long friendId);
}
