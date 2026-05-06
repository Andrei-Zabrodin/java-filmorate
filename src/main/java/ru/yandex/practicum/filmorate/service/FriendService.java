package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.friend.FriendsStorage;

import java.time.Instant;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendsStorage friendsStorage;
    private final EventStorage eventStorage;

    public Collection<User> getFriends(int userId) {
        log.debug("Возвращаем список друзей пользователя с id {}", userId);
        return friendsStorage.getFriends(userId);
    }

    public Collection<User> getCommonFriends(int userId, int otherId) {
        log.debug("Возвращаем общих друзей пользователей с id {} и {}", userId, otherId);
        return friendsStorage.getCommonFriends(userId, otherId);
    }

    public void addFriend(int userId, int friendId) {
        log.debug("Пользователю с id {} добавляем друга с id {}", userId, friendId);
        friendsStorage.addFriend(userId, friendId);

        Event event = Event.builder()
                .userId(userId)
                .timestamp(Instant.now().toEpochMilli())
                .eventType(EventType.FRIEND)
                .operation(OperationType.ADD)
                .entityId(friendId)
                .build();
        eventStorage.addEvent(event);
    }

    public void deleteFriend(int userId, int friendId) {
        log.debug("У пользователя с id {} убираем из друзей пользователя с id {}", userId, friendId);
        friendsStorage.deleteFriend(userId, friendId);

        Event event = Event.builder()
                .userId(userId)
                .timestamp(Instant.now().toEpochMilli())
                .eventType(EventType.FRIEND)
                .operation(OperationType.REMOVE)
                .entityId(friendId)
                .build();
        eventStorage.addEvent(event);
    }
}
