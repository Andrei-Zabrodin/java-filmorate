package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DatabaseException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
public class UserDbStorage extends DbStorage<User> implements UserStorage {
    private static final String GET_ALL_USERS_QUERY = "SELECT * FROM users";
    private static final String GET_USER_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String ADD_USER_QUERY = "INSERT INTO users(login, email, name, birthday)" +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER_QUERY_START = "UPDATE users SET ";
    private static final String DELETE_USER_QUERY = "DELETE FROM users WHERE user_id = ?";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<User> getUsers() {
        log.debug("Возвращаем список всех пользователей");
        return findMany(GET_ALL_USERS_QUERY);
    }

    @Override
    public User getUserById(int id) {
        Optional<User> user = findOne(GET_USER_BY_ID_QUERY, id);
        if (user.isPresent()) {
            log.debug("Найден пользователь с id {}", id);
            return user.get();
        } else {
            log.debug("Не удалось найти пользователя с указанным id");
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }

    @Override
    public User addUser(User user) {
        Optional<Integer> id = insert(ADD_USER_QUERY,
                user.getLogin(),
                user.getEmail(),
                user.getName(),
                user.getBirthday());

        if (id.isPresent()) {
            user.setId(id.get());
            log.debug("Добавлен пользователь с id {}", user.getId());
            return user;
        } else {
            throw new DatabaseException("Не удалось добавить данные");
        }

    }

    @Override
    public User updateUser(User newUser) {
        StringBuilder query = new StringBuilder(UPDATE_USER_QUERY_START);
        List<Object> params = new ArrayList<>();

        if (newUser.getLogin() != null) {
            query.append("login = ?, ");
            params.add(newUser.getLogin());
        }

        if (newUser.getEmail() != null) {
            query.append("email = ?, ");
            params.add(newUser.getEmail());
        }

        if (newUser.getName() != null) {
            query.append("name = ?, ");
            params.add(newUser.getName());
        }

        if (newUser.getBirthday() != null) {
            query.append("birthday = ?, ");
            params.add(newUser.getBirthday());
        }

        query.setLength(query.length() - 2); //Убираем пробел и запятую с конца
        query.append(" WHERE user_id = ?");
        params.add(newUser.getId());

        log.debug("Итоговые параметры для обновления: {}", params);
        update(query.toString(), params.toArray());

        User user = getUserById(newUser.getId());
        log.debug("Пользователь с id {} обновлен", newUser.getId());
        return user;
    }

    @Override
    public User deleteUser(int id) {
        User user = getUserById(id);
        log.debug("Удаляем пользователя с id {}", id);
        delete(DELETE_USER_QUERY, id);

        return user;
    }
}