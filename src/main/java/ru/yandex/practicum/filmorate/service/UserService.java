package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public Collection<User> getFriends(int userId) {
        log.debug("Возвращаем список друзей пользователя с id {}", userId);
        return userStorage.getUserById(userId).getFriends();
    }

    public Collection<User> getCommonFriends(int userId, int otherId) {
        Collection<User> userFriends = userStorage.getUserById(userId).getFriends();
        Collection<User> otherUserFriends = userStorage.getUserById(otherId).getFriends();
        userFriends.retainAll(otherUserFriends);

        log.debug("Возвращаем общих друзей пользователей с id {} и {}", userId, otherId);
        return userFriends;
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
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        user.addFriend(friend);
        friend.addFriend(user);
    }

    public void deleteFriend(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        user.removeFriend(friend);
        friend.removeFriend(user);
    }

    private void validateUser(User user) {
        if (user.getId() == 0) {
            log.debug("В запросе не указан id пользователя");
            throw new ValidateException("Необходимо указать id пользователя!");
        }
    }
}
