package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface FriendsStorage {
    Collection<User> getFriends(int userId);

    Collection<User> getCommonFriends(int userId, int otherId);

    void addFriend(int userId, int friendId);

    void deleteFriend(int userId, int friendId);
}