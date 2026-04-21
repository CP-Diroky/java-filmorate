package ru.yandex.practicum.filmorate.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;


@Service
public class UserService {
    private final UserStorage userStorage;
    private final Logger log = LoggerFactory.getLogger(UserService.class);
    private final EventStorage eventStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("eventDbStorage") EventStorage eventStorage) {
        this.userStorage = userStorage;
        this.eventStorage = eventStorage;
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
        eventStorage.addEvent(id, friendId, Event.EventType.FRIEND, Event.Operation.ADD);
        return userStorage.addFriend(id, friendId);
    }

    public User deleteFriend(Long id, Long friendId) {
        log.info("Пользователь {} удален из списка друзей", friendId);
        eventStorage.addEvent(id, friendId, Event.EventType.FRIEND, Event.Operation.REMOVE);
        return userStorage.deleteFriend(id, friendId);
    }

    public Collection<User> getAllFriends(Long id) {
        log.info("Список друзей получен");
        return userStorage.getAllFriends(id);
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }

    public Collection<Event> getFeed(Long id) {
        return eventStorage.getFeed(id);
    }
}
