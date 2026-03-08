package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Integer, User> users = new HashMap<>();
    private int currentId = 0;

    @GetMapping
    public Collection<User> getUsers() {
        log.debug("Возвращён список пользователей");
        return users.values();
    }

    @PostMapping
    public User postUser(@Valid @RequestBody User user) {
        user.setId(++currentId);

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Пользователю установили логин {} в качестве имени", user.getLogin());
        }

        users.put(user.getId(), user);
        log.debug("Добавлен пользователь с id {}", user.getId());
        return user;
    }

    @PutMapping()
    public User updateUser(@Valid @RequestBody User newUser) {
        log.debug("Начинаем исправление записи с id пользователя {}", newUser.getId());
        if (newUser.getId() == 0) {
            log.debug("В запросе не указан id пользователя");
            throw new ValidateException("Необходимо указать id пользователя!");
        }

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            Optional.ofNullable(newUser.getEmail()).ifPresent(oldUser::setEmail);
            Optional.ofNullable(newUser.getLogin()).ifPresent(oldUser::setLogin);
            Optional.ofNullable(newUser.getName()).ifPresent(oldUser::setName);
            Optional.ofNullable(newUser.getBirthday()).ifPresent(oldUser::setBirthday);

            log.debug("Пользователь с id {} обновлен", newUser.getId());
            return oldUser;
        }

        log.debug("Не удалось найти пользователя с указанным id");
        throw new ValidateException("Пользователь с id = " + newUser.getId() + " не найден");
    }
}
