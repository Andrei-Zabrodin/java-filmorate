package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
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

    private void validateUser(User user) {
        if (user.getId() == 0) {
            log.debug("В запросе не указан id пользователя");
            throw new ValidateException("Необходимо указать id пользователя!");
        }
    }
}