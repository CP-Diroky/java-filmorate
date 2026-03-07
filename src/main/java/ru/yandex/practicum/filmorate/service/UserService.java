package ru.yandex.practicum.filmorate.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
public class UserService {
    private final UserStorage userStorage;
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    public UserService(UserStorage userStorage) {
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
        User friend = userStorage.getUserById(friendId); //Проверяем есть ли такой id
        User user = userStorage.getUserById(id);
        if (userStorage.getUserById(id).getFriends().contains(friendId) &&
                userStorage.getUserById(friendId).getFriends().contains(id)) {
            log.info("Друг уже добавлен");
            return user;
        }
        userStorage.getUserById(id).getFriends().add(friendId);
        userStorage.getUserById(friendId).getFriends().add(id);
        log.info("Друг добавлен");
        return user;
    }

    public User deleteFriend(Long id, Long friendId) {
        User friend = userStorage.getUserById(friendId); //Проверяем есть ли такой id
        User user = userStorage.getUserById(id);
        if (!userStorage.getUserById(id).getFriends().contains(friendId) &&
                !userStorage.getUserById(friendId).getFriends().contains(id)) {
            log.info("Друг уже удален");
            return user;
        }
        userStorage.getUserById(id).getFriends().remove(friendId);
        userStorage.getUserById(friendId).getFriends().remove(id);
        log.info("Пользователь {} удален из списка друзей", friendId);
        return user;
    }

    public Collection<User> getAllFriends(Long id) {
        List<User> friends = userStorage.getUserById(id).getFriends()
                .stream()
                .map(userStorage::getUserById)
                .toList();
        log.info("Список друзей получен");
        return friends;
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        Set<Long> otherIdFriends = userStorage.getUserById(otherId).getFriends();
        List<User> commonFriends = userStorage.getUserById(id).getFriends()
                .stream()
                .filter(otherIdFriends::contains)
                .map(userStorage::getUserById).toList();
        return commonFriends;
    }


}
