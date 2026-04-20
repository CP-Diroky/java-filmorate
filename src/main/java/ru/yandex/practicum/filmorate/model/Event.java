package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Event {
    Long id;
    Long user_id;
    Long timestamp;
    EventType eventType;
    Operation operation;
    Long entityId;

    public enum EventType {
        LIKE, REVIEW, FRIEND
    }

    public enum Operation {
        ADD, REMOVE, UPDATE
    }
}
