package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Event {
    private Long eventId;
    private Long userId;
    private Long timestamp;
    private EventType eventType;
    private Operation operation;
    private Long entityId;

    public enum EventType {
        LIKE, REVIEW, FRIEND
    }

    public enum Operation {
        ADD, REMOVE, UPDATE
    }
}
