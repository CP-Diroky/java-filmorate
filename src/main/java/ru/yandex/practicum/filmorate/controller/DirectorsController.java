package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorsStorage;

import java.util.Collection;

@RestController
@Validated
@RequestMapping("/directors")
public class DirectorsController {

    private final DirectorsStorage directorsStorage;

    public DirectorsController(@Qualifier("directorDbStorage") DirectorsStorage directorsStorage) {
        this.directorsStorage = directorsStorage;
    }

    @GetMapping
    public Collection<Director> getAllDirectors() {
        return directorsStorage.getAllDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable @Positive Long id) {
        return directorsStorage.getDirectorById(id);
    }

    @PostMapping
    public Director addDirector(@RequestBody @Valid Director director) {
        return directorsStorage.addDirector(director);
    }

    @PutMapping
    public Director changeDirector(@RequestBody Director director) {
        return directorsStorage.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable @Positive Long id) {
        directorsStorage.deleteDirector(id);
    }

}
