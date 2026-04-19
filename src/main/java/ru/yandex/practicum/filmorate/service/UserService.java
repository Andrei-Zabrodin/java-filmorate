package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendsStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendsStorage friendsStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, FriendsStorage friendsStorage) {
        this.userStorage = userStorage;
        this.friendsStorage = friendsStorage;
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public Collection<User> getFriends(int userId) {
        log.debug("Возвращаем список друзей пользователя с id {}", userId);
        return friendsStorage.getFriends(userId);
    }

    public Collection<User> getCommonFriends(int userId, int otherId) {
        log.debug("Возвращаем общих друзей пользователей с id {} и {}", userId, otherId);
        return friendsStorage.getCommonFriends(userId, otherId);
    }

    public User addUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Пользователю установили логин {} в качестве имени", user.getLogin());
        }

        return userStorage.addUser(user);
    }

    public User updateUser(User newUser) {
        log.debug("Начинаем исправление записи с id пользователя {}", newUser.getId());
        validateUser(newUser);

        return userStorage.updateUser(newUser);
    }

    public User deleteUser(int id) {
        return userStorage.deleteUser(id);
    }

    public void addFriend(int userId, int friendId) {
        log.debug("Пользователю с id {} добавляем друга с id {}", userId, friendId);
        friendsStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(int userId, int friendId) {
        log.debug("У пользователя с id {} убираем из друзей пользователя с id {}", userId, friendId);
        friendsStorage.deleteFriend(userId, friendId);
    }

    private void validateUser(User user) {
        if (user.getId() == 0) {
            log.debug("В запросе не указан id пользователя");
            throw new ValidateException("Необходимо указать id пользователя!");
        }
    }
}
