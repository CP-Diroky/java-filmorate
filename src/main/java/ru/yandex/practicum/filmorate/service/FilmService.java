package ru.yandex.practicum.filmorate.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final Logger log = LoggerFactory.getLogger(FilmService.class);
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
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
        User userThatLiked = userStorage.getUserById(userId); //Проверяем есть ли такой пользователь
        Film likedFilm = filmStorage.getFilmById(id);
        likedFilm.getUsersId().add(userId);
        return likedFilm;
    }

    public Film deleteLike(Long id, Long userId) {
        User userThatLiked = userStorage.getUserById(userId); //Проверяем есть ли такой пользователь
        Film likedFilm = filmStorage.getFilmById(id);
        likedFilm.getUsersId().remove(userId);
        return likedFilm;
    }

    public Collection<Film> getPopularFilms(int count) {
        List<Film> filmsFiltered;
        if (count > 0) {
            filmsFiltered = filmStorage.getAllFilms().stream()
                    .sorted((film1, film2) -> {
                        return film2.getUsersId().size() - film1.getUsersId().size();
                    })
                    .limit(count).toList();
        } else {
            filmsFiltered = filmStorage.getAllFilms().stream()
                    .sorted((film1, film2) -> {
                        return film2.getUsersId().size() - film1.getUsersId().size();
                    })
                    .limit(10).toList();
        }
        return filmsFiltered;
    }

}
