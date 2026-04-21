package ru.yandex.practicum.filmorate.service;


import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("genreDbStorage") GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film changeFilm(Film film) {
        return filmStorage.changeFilm(film);
    }

    public Film getFilmById(Long id) {
        return filmStorage.getFilmById(id);
    }

    public Film addLike(Long id, Long userId) {
        userStorage.getUserById(userId);
        filmStorage.getFilmById(id);
        return filmStorage.addLike(id, userId);
    }

    public Film deleteLike(Long id, Long userId) {
        userStorage.getUserById(userId);
        filmStorage.getFilmById(id);
        return filmStorage.deleteLike(id, userId);
    }


    public List<Film> getPopularFilms(int count, Long genreId, Integer year) {
        if (count < 1) {
            count = 10;
        }
        if (genreId != null) {
            genreStorage.getGenreById(genreId);
        }
        return filmStorage.getPopularFilms(count, genreId, year);
    }

    public Collection<Film> getDirectorsFilms(Long directorId, String sortType) {
        return switch (sortType) {
            case "year" -> filmStorage.getSortDirectorsFilmsByYear(directorId);
            case "likes" -> filmStorage.getSortDirectorsFilmsByLikes(directorId);
            default -> throw new NotFoundException("Тип сортировки не найден");
        };
    }

    public Collection<Film> getSearchedFilms(String query, String searchByTypes) {
        Collection<Film> films = new ArrayList<>();

        for (String searchByType : searchByTypes.split(",")) {
            switch (searchByType) {
                case "director" -> films.addAll(filmStorage.getSearchedFilmsByDirector(query));
                case "title" -> films.addAll(filmStorage.getSearchedFilmsByTitle(query));
                default -> throw new NotFoundException("Тип поиска не найден");
            }
        }

        return films;
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);
        return filmStorage.getCommonFilms(userId, friendId);
    }
    
    public void deleteFilmById(@Positive Long filmId) {
        getFilmById(filmId);
        filmStorage.deleteFilmById(filmId);
    }
}
