package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;

@Repository
@Qualifier("eventDbStorage")
public class EventDbStorage implements EventStorage {

    private final JdbcTemplate jdbcTemplate;


    public EventDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addEvent(Long userId, Long entityId, Event.EventType eventType, Event.Operation operation) {
        Long timestamp = Instant.now().toEpochMilli();

        String sql = "INSERT INTO events (user_id, timestamp, event_type, operation, entity_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql, userId, timestamp, eventType.name(), operation.name(), entityId);
    }

    @Override
    public Collection<Event> getFeed(Long id) {

        String sql = """
                SELECT e.*
                FROM events e
                JOIN friends f ON e.user_id = f.friend_id
                WHERE f.user_id = ?
                """;
        return jdbcTemplate.query(sql, this::mapRowToEvent, id);

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
}
