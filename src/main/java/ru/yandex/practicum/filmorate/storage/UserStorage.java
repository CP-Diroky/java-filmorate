package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    Collection<User> getAllUsers();

    User addUser(User user);

    User changeUser(User user);

    User getUserById(Long id);

    User addFriend(Long id, Long friendId);

    User deleteFriend(Long id, Long friendId);

    Collection<User> getAllFriends(Long id);

    Collection<User> getCommonFriends(Long id, Long otherId);
}
