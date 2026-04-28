package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private int currentId = 0;

    @Override
    public Collection<User> getUsers() {
        log.debug("Возвращаем список пользователей");
        return users.values();
    }

    @Override
    public User getUserById(int id) {
        checkUserId(id);

        log.debug("Найден пользователь с id {}", id);
        return users.get(id);
    }

    @Override
    public User addUser(User user) {
        user.setId(++currentId);
        users.put(user.getId(), user);

        log.debug("Добавлен пользователь с id {}", user.getId());
        return user;
    }

    @Override
    public User updateUser(User newUser) {
        checkUserId(newUser.getId());

        User oldUser = users.get(newUser.getId());

        Optional.ofNullable(newUser.getEmail()).ifPresent(oldUser::setEmail);
        Optional.ofNullable(newUser.getLogin()).ifPresent(oldUser::setLogin);
        Optional.ofNullable(newUser.getName()).ifPresent(oldUser::setName);
        Optional.ofNullable(newUser.getBirthday()).ifPresent(oldUser::setBirthday);

        log.debug("Пользователь с id {} обновлен", newUser.getId());
        return oldUser;
    }

    @Override
    public User deleteUser(int id) {
        checkUserId(id);

        log.debug("Удаляем пользователя с id {}", id);
        return users.remove(id);
    }

    public void checkUserId(int id) {
        if (!users.containsKey(id)) {
            log.debug("Не удалось найти пользователя с указанным id");
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }
}