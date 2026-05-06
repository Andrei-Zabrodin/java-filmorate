package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private int eventId;
    private long timestamp;
    private int userId;
    private EventType eventType; // LIKE, REVIEW или FRIEND
    private OperationType operation; // REMOVE, ADD, UPDATE
    private int entityId; // идентификатор сущности, с которой произошло событие
}
