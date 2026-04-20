package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FriendService;

import java.util.Collection;

@RestController
@RequestMapping("/users/{id}/friends")
@Slf4j
@RequiredArgsConstructor
public class FriendsController {
    private final FriendService friendService;

    @GetMapping
    public Collection<User> getFriends(@PathVariable int id) {
        return friendService.getFriends(id);
    }

    @GetMapping("/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable int id, @PathVariable int otherId) {
        return friendService.getCommonFriends(id, otherId);
    }

    @PutMapping("/{friendId}")
    public void addFriend(@PathVariable int id, @PathVariable int friendId) {
        friendService.addFriend(id, friendId);
    }

    @DeleteMapping("/{friendId}")
    public void deleteFriend(@PathVariable int id, @PathVariable int friendId) {
        friendService.deleteFriend(id, friendId);
    }
}
