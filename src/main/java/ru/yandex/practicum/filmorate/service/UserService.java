package ru.yandex.practicum.filmorate.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
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

    public User sendFriendshipRequest(Long id, Long friendId) {
        User user = checkFriendshipStatus(id, friendId);
        userStorage.getUserById(id).getFriendshipStatusMap().put(friendId, FriendshipStatus.NOT_ACCEPTED);
        userStorage.getUserById(friendId).getFriendshipStatusMap().put(id, FriendshipStatus.NOT_ACCEPTED);
        log.info("Заявка отправлена");
        return user;
    }

    public User acceptFriendshipRequest(Long id, Long friendId) {
        User user = checkFriendshipStatus(id, friendId);
        userStorage.getUserById(id).getFriendshipStatusMap().put(friendId, FriendshipStatus.ACCEPTED);
        userStorage.getUserById(friendId).getFriendshipStatusMap().put(id, FriendshipStatus.ACCEPTED);
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
        userStorage.getUserById(id).getFriendshipStatusMap().remove(friendId);
        userStorage.getUserById(friendId).getFriendshipStatusMap().remove(id);
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

    public User checkFriendshipStatus(Long id, Long friendId) {
        User friend = userStorage.getUserById(friendId); //Проверяем есть ли такой id
        User user = userStorage.getUserById(id);
        if (userStorage.getUserById(id).getFriends().contains(friendId) &&
                userStorage.getUserById(friendId).getFriends().contains(id)) {
            log.info("Друг уже добавлен");
            throw new ConditionsNotMetException("Друг уже добавлен");
        } else if (userStorage.getUserById(id).getFriendshipStatusMap().get(friendId) == FriendshipStatus.ACCEPTED) {
            log.info("Друг уже добавлен");
            throw new ConditionsNotMetException("Друг уже добавлен");
        } else if (id.equals(friendId)) {
            log.info("Нельзя добавить себя в друзья");
            throw new ConditionsNotMetException("Нельзя добавить себя в друзья");
        } else {
            return user;
        }
    }


}
