package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.validator.ValidReleaseDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@Slf4j
public class Film {

    private static final int MAX_DESCRIPTION = 200;
    private int id;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private Set<User> thoseWhoLiked = new HashSet<>();

    @NotBlank(message = "У фильма должно быть заполнено название")
    private String name;

    @Size(max = MAX_DESCRIPTION, message = "Описание фильма не должно превышать 200 символов!")
    private String description;

    @ValidReleaseDate
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть больше 0!")
    private Integer duration;

    private Set<Genre> genres = new HashSet<>();
    private Rating mpa;

    @JsonIgnore
    public int getLikesAmount() {
        return this.getThoseWhoLiked().size();
    }

    public void addLike(User user) {
        log.debug("Ставим лайк фильму с id {} от пользователя с id {}", this.id, user.getId());
        thoseWhoLiked.add(user);
    }

    public void removeLike(User user) {
        log.debug("Удаляем фильму с id {} лайк от пользователя с id {}", this.id, user.getId());
        thoseWhoLiked.remove(user);
    }
}
