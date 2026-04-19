package ru.yandex.practicum.filmorate.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
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
        userStorage.getUserById(userId); //Проверяем есть ли такой пользователь
        filmStorage.getFilmById(id);
        return filmStorage.addLike(id,userId);
    }

    public Film deleteLike(Long id, Long userId) {
        userStorage.getUserById(userId);
        filmStorage.getFilmById(id);
        return filmStorage.deleteLike(id, userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        if (count < 1) {
            count = 10;
        }
        return filmStorage.getPopularFilms(count);
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
            log.info(searchByType);

            log.info(searchByTypes);

            switch (searchByType) {
                case "director" -> films.addAll(filmStorage.getSearchedFilmsByDirector(query));
                case "title" -> films.addAll(filmStorage.getSearchedFilmsByTitle(query));
                default -> throw new NotFoundException("Тип поиска не найден");
            }
        }

        return films;
    }

}
