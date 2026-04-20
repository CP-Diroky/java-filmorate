package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Repository
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final Logger log = LoggerFactory.getLogger(UserDbStorage.class);
    private final EventDbStorage eventDbStorage;

    public UserDbStorage(JdbcTemplate jdbcTemplate, EventDbStorage eventDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.eventDbStorage = eventDbStorage;
    }

    @Override
    public Collection<User> getAllUsers() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    @Override
    public User getUserById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToUser, id);
        } catch (RuntimeException e) {
            throw new NotFoundException("Пользователь не найден");
        }
    }

    @Override
    public User addUser(User user) {

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

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        String sql = "INSERT INTO users(email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User changeUser(User user) {
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

        getUserById(user.getId()); //Проверяем что пользователь есть

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        String sql = "UPDATE users SET email=?, login=?, name=?, birthday=? WHERE id=?";

        int rows = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());

        if (rows == 0) {
            throw new NotFoundException("Пользователь не найден");
        }

        return user;
    }

    @Override
    public User addFriend(Long id, Long friendId) {

        getUserById(id); //Проверяем что пользователь есть
        getUserById(friendId);

        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, id, friendId);
        eventDbStorage.addEvent(sql, id, friendId, Event.EventType.FRIEND);
        return getUserById(id);
    }

    @Override
    public User deleteFriend(Long id, Long friendId) {

        getUserById(id); //Проверяем что пользователь есть
        getUserById(friendId);

        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, id, friendId);
        eventDbStorage.addEvent(sql, id, friendId, Event.EventType.FRIEND);
        return getUserById(id);
    }

    @Override
    public Collection<User> getAllFriends(Long id) {

        getUserById(id);

        String sql = """
                SELECT u.*
                FROM users u
                JOIN friends f ON u.id = f.friend_id
                WHERE f.user_id = ?
                """;

        return jdbcTemplate.query(sql, this::mapRowToUser, id);
    }

    @Override
    public Collection<User> getCommonFriends(Long id, Long otherId) {

        getUserById(id); //Проверяем что пользователь есть
        getUserById(otherId);

        String sql = """
                SELECT u.*
                FROM users u
                JOIN friends f1 ON u.id = f1.friend_id
                JOIN friends f2 ON u.id = f2.friend_id
                WHERE f1.user_id = ? AND f2.user_id = ?
                """;

        return jdbcTemplate.query(sql, this::mapRowToUser, id, otherId);
    }

    @Override
    public Collection<Event> getFeed(Long id) {
        getUserById(id);

        String sql = """
                SELECT e.*
                FROM events e
                JOIN friends f ON e.user_id = f.friend_id
                WHERE f.user_id = ?
                """;
        return jdbcTemplate.query(sql, this::mapRowToEvent, id);

    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setFriends(getFriendsIds(user.getId()));
        Date birthday = rs.getDate("birthday");
        if (birthday != null) {
            user.setBirthday(birthday.toLocalDate());
        }

        return user;
    }

    private Event mapRowToEvent(ResultSet rs, int rownum) throws SQLException {
        Event event = new Event();
        event.setId(rs.getLong("id"));
        event.setUser_id(rs.getLong("user_id"));
        event.setTimestamp(rs.getLong("timestamp"));
        event.setEventType(Event.EventType.valueOf(rs.getString("event_type")));
        event.setOperation(Event.Operation.valueOf(rs.getString("operation")));
        event.setEntityId(rs.getLong("entity_id"));
        return event;
    }

    private Set<Long> getFriendsIds(Long userId) {
        String sql = "SELECT friend_id FROM friends WHERE user_id = ?";

        return new HashSet<>(jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("friend_id"),
                userId
        ));
    }

}
