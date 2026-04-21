package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

public interface DirectorsStorage {

    Collection<Director> getAllDirectors();

    Director getDirectorById(Long id);

    void deleteDirector(Long id);

    Director addDirector(Director director);

    Director updateDirector(Director director);

}
