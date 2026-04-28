package ru.yandex.practicum.filmorate.storage.friend;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.DbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Slf4j
@Repository
public class FriendDbStorage extends DbStorage<User> implements FriendsStorage {
    private static final String GET_ALL_FRIENDS_QUERY = "SELECT * FROM users " +
            "WHERE user_id IN (SELECT friend_id FROM friends WHERE user_id = ?)";
    private static final String GET_COMMON_FRIENDS_QUERY = "SELECT * FROM users WHERE user_id IN " +
            "(SELECT friend_id FROM friends WHERE user_id IN (?, ?) GROUP BY friend_id HAVING COUNT(friend_id) > 1)";
    private static final String ADD_FRIEND_QUERY = "INSERT INTO friends(user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";

    private final UserStorage userStorage;

    public FriendDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper,
                           @Qualifier("userDbStorage") UserStorage userStorage) {
        super(jdbc, mapper);
        this.userStorage = userStorage;
    }

    @Override
    public Collection<User> getFriends(int userId) {
        checkUserId(userId);

        return findMany(GET_ALL_FRIENDS_QUERY, userId);
    }

    @Override
    public Collection<User> getCommonFriends(int userId, int otherId) {
        checkUserId(userId);
        checkUserId(otherId);

        return findMany(GET_COMMON_FRIENDS_QUERY, userId, otherId);
    }

    @Override
    public void addFriend(int userId, int friendId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);

        simpleInsert(ADD_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        checkUserId(userId);
        checkUserId(friendId);
        delete(DELETE_FRIEND_QUERY, userId, friendId);
    }

    private void checkUserId(int userId) {
        userStorage.getUserById(userId);
    }
}