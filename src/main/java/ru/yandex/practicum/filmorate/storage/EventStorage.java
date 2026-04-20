package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;

public interface EventStorage {

    void addEvent(String sql, Long userId, Long entityId, Event.EventType eventType);

}
