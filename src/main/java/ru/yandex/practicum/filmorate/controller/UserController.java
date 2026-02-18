package ru.yandex.practicum.filmorate.controller;


import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    Map<Long, User> users = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(UserController.class);


    public long getNextId() {
        long id = users.values().stream()
                .mapToLong(User::getId)
                .max().orElse(0);
        id++;
        return id;
    }

    @GetMapping
    public Collection<User> getAllFilms() {
        log.info("Список пользователей получен");
        return users.values();
    }


    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        String errorMessage;
        if (user == null || user.getEmail().isBlank()) {
            errorMessage = "Почта не должна быть пустой!";
            log.error(errorMessage);
            throw new ConditionsNotMetException(errorMessage);
        } else if (user.getLogin().isBlank()) {
            errorMessage = "Логин не может быть пустым!";
            log.error(errorMessage);
            throw new ConditionsNotMetException(errorMessage);
        } else if (user.getBirthday().isAfter(LocalDate.now())) {
            errorMessage = "Дата рождения не может быть в будущем!";
            log.error(errorMessage);
            throw new ConditionsNotMetException(errorMessage);
        }
        user.setId(getNextId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Пользователь добавлен");
        return user;
    }

    @PutMapping
    public User changeUser(@Valid @RequestBody User user) {
        String errorMessage;
        if (user == null || !users.containsKey(user.getId())) {
            errorMessage = "Пользователь не найден!";
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        User newUser = users.get(user.getId());
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            newUser.setEmail(user.getEmail());
        }
        if (user.getLogin() != null && !user.getLogin().isBlank()) {
            newUser.setLogin(user.getLogin());
        }
        if (user.getName() != null && !user.getName().isBlank()) {
            newUser.setName(user.getName());
        }
        if (user.getBirthday() != null && !user.getBirthday().isAfter(LocalDate.now())) {
            newUser.setBirthday(user.getBirthday());
        }
        users.put(newUser.getId(), newUser);
        log.info("Данные пользователя обновлены");
        return newUser;
    }
}
