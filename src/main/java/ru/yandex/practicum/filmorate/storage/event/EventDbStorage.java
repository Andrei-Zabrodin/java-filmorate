package ru.yandex.practicum.filmorate.storage.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DatabaseException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.storage.DbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.Instant;
import java.util.Collection;

@Slf4j
@Repository
public class EventDbStorage extends DbStorage<Event> implements EventStorage {
    private static final String GET_USER_EVENTS_QUERY = "SELECT * FROM events WHERE user_id = ? ORDER BY event_id";
    private static final String ADD_USER_EVENT_QUERY = "INSERT INTO events(event_timestamp, user_id, event_type, " +
            "operation, entity_id) VALUES(?, ?, ?, ?, ?)";

    private final UserStorage userStorage;

    public EventDbStorage(JdbcTemplate jdbc, RowMapper<Event> mapper,
                          @Qualifier("userDbStorage") UserStorage userStorage) {
        super(jdbc, mapper);
        this.userStorage = userStorage;
    }

    @Override
    public Collection<Event> getEvents(int userId) {
        userStorage.checkUserExistence(userId);

        log.debug("Возвращаем ленту событий пользователя с id {}", userId);
        return findMany(GET_USER_EVENTS_QUERY, userId);
    }

    @Override
    public void addEvent(int userId, EventType eventType, OperationType operation, int entityId) {
        log.debug("Добавляем событие {} {} сущности с id {} от пользователя с id {}",
                operation, eventType, entityId, userId);

        int id = insert(ADD_USER_EVENT_QUERY, Instant.now(), userId, eventType.toString(),
                operation.toString(), entityId)
                .orElseThrow(() -> new DatabaseException("Не удалось добавить данные"));

        log.debug("Добавлено событие с id {}", id);
    }
}
