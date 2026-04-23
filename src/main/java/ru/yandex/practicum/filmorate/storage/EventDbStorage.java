package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;

@Repository
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
            SELECT *
            FROM events
            WHERE user_id = ?
            ORDER BY event_id
            """;

        return jdbcTemplate.query(sql, this::mapRowToEvent, id);
    }

    private Event mapRowToEvent(ResultSet rs, int rownum) throws SQLException {
        Event event = new Event();
        event.setEventId(rs.getLong("event_id"));
        event.setUserId(rs.getLong("user_id"));
        event.setTimestamp(rs.getLong("timestamp"));
        event.setEventType(Event.EventType.valueOf(rs.getString("event_type")));
        event.setOperation(Event.Operation.valueOf(rs.getString("operation")));
        event.setEntityId(rs.getLong("entity_id"));
        return event;
    }
}
