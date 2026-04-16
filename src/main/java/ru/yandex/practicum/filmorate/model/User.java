package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Slf4j
public class User {
    private int id;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private Set<User> friends = new HashSet<>();

    @NotBlank(message = "Электронная почта не должна быть пустой!")
    @Email(message = "Некорректный формат электронной почты!")
    private String email;

    @NotBlank(message = "Логин не должен быть пустой!")
    @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы!")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения должна быть не позже текущей даты!")
    private LocalDate birthday;

    public void addFriend(User user) {
        log.debug("Добавляем пользователя с id {} в друзья к пользователю {}", user.getId(), this.id);
        friends.add(user);
    }

    public void removeFriend(User user) {
        log.debug("Удаляем пользователя с id {} из друзей пользователю {}", user.getId(), this.id);
        friends.remove(user);
    }
}
