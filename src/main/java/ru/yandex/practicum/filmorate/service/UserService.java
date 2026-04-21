package ru.yandex.practicum.filmorate.service;


import jakarta.validation.constraints.Positive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;


@Service
public class UserService {
    private final UserStorage userStorage;
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User changeUser(User user) {
        return userStorage.changeUser(user);
    }

    public User getUserById(Long id) {
        return userStorage.getUserById(id);
    }

    public User addFriend(Long id, Long friendId) {
        if (id.equals(friendId)) {
            throw new ConditionsNotMetException("Нельзя добавить себя в друзья");
        }
        log.info("Друг добавлен");
        return userStorage.addFriend(id, friendId);
    }

    public User deleteFriend(Long id, Long friendId) {
        log.info("Пользователь {} удален из списка друзей", friendId);
        return userStorage.deleteFriend(id, friendId);
    }

    public Collection<User> getAllFriends(Long id) {
        log.info("Список друзей получен");
        return userStorage.getAllFriends(id);
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }

    public void deleteUserById(@Positive Long userId) {
        userStorage.deleteUser(userId);
    }
}
