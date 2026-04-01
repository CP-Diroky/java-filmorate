package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;
//Класс для хранения данных пользователей в памяти, а не в БД
@Component
public class InMemoryUserStorage implements UserStorage {
    private Map<Long, User> users = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(InMemoryUserStorage.class);


    public long getNextId() {
        long id = users.values().stream()
                .mapToLong(User::getId)
                .max().orElse(0);
        id++;
        return id;
    }

    @Override
    public Collection<User> getAllUsers() {
        log.info("Список пользователей получен");
        return users.values();
    }

    @Override
    public User addUser(@Valid User user) {
        String errorMessage;
        if (user == null) {
            errorMessage = "Неверный ввод!";
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

    @Override
    public User changeUser(User user) {
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


    @Override
    public User getUserById(Long id) {
        if (id == null || !users.containsKey(id)) {
            throw new NotFoundException("Пользователь не найден!");
        }
        return users.get(id);
    }

    @Override
    public User addFriend(Long id, Long friendId) {
        User friend = users.get(friendId); //Проверяем есть ли такой id
        User user = users.get(id);
        if (user.getFriends().contains(friendId)) {
            log.info("Друг уже добавлен");
            return user;
        }
        user.getFriends().add(friendId);
        return user;
    }

    @Override
    public User deleteFriend(Long id, Long friendId) {
        User friend = users.get(friendId); //Проверяем есть ли такой id
        User user = users.get(id);
        if (!user.getFriends().contains(friendId)) {
            log.info("Пользователя уже нет в друзьях");
            return user;
        }
        user.getFriends().remove(friendId);
        return user;
    }

    @Override
    public Collection<User> getAllFriends(Long id) {
        List<User> friends = users.get(id).getFriends()
                .stream()
                .map(users::get)
                .toList();
        return friends;
    }

    @Override
    public Collection<User> getCommonFriends(Long id, Long otherId) {
        Set<Long> otherIdFriends = users.get(otherId).getFriends();
        List<User> commonFriends = users.get(id).getFriends()
                .stream()
                .filter(otherIdFriends::contains)
                .map(users::get).toList();
        return commonFriends;
    }
}
