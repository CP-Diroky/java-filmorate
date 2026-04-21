package ru.yandex.practicum.filmorate.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final EventStorage eventStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("eventDbStorage") EventStorage eventStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.eventStorage = eventStorage;
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
        eventStorage.addEvent(userId, id, Event.EventType.LIKE, Event.Operation.ADD);
        return filmStorage.addLike(id,userId);
    }

    public Film deleteLike(Long id, Long userId) {
        userStorage.getUserById(userId);
        filmStorage.getFilmById(id);
        eventStorage.addEvent(userId, id, Event.EventType.LIKE, Event.Operation.REMOVE);
        return filmStorage.deleteLike(id, userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        if (count < 1) {
            count = 10;
        }
        return filmStorage.getPopularFilms(count);
    }

}
