package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.Collection;

public interface EventStorage {

    void addEvent(Long userId, Long entityId, Event.EventType eventType, Event.Operation operation);

    Collection<Event> getFeed(Long id);

}
