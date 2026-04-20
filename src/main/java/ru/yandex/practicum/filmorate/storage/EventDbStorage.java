package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

import java.time.Instant;

@Repository
public class EventDbStorage implements EventStorage {

    private final JdbcTemplate jdbcTemplate;


    public EventDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void addEvent(String sql, Long userId, Long entityId, Event.EventType eventType) {
        String method = sql.trim().split(" ")[0];
        Event.Operation operation;
        Long timestamp = Instant.now().toEpochMilli();
        switch (method) {
            case "INSERT":
                operation = Event.Operation.ADD;
                break;
            case "DELETE":
                operation = Event.Operation.REMOVE;
                break;
            case "UPDATE":
                operation = Event.Operation.UPDATE;
                break;
            default:
                throw new IllegalArgumentException("Неверный метод");
        }
        String event = "INSERT INTO events (user_id, timestamp, event_type, operation, entity_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(event, userId, timestamp, eventType.name(), operation.name(), entityId);
    }
}
