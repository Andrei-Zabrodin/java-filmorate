package ru.yandex.practicum.filmorate.storage.event;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;

import java.util.Collection;

public interface EventStorage {
    Collection<Event> getEvents(int userId);

    void addEvent(int userId, EventType eventType, OperationType operation, int entityId);
}
