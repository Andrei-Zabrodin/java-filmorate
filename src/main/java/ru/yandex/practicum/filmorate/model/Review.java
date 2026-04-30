package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Data
@Slf4j
public class Review {
    private int reviewId;

    @NotBlank(message = "Текст отзыва не должен быть пустым!")
    @Size(max = 1000, message = "Отзыв не должен превышать 1000 символов!")
    private String content;

    @NotNull(message = "Тип отзыва должен быть указан!")
    private Boolean isPositive; // true = положительный, false = отрицательный

    @NotNull(message = "ID пользователя должен быть указан!")
    @Positive(message = "ID пользователя должен быть положительным!")
    private Integer userId;

    @NotNull(message = "ID фильма должен быть указан!")
    @Positive(message = "ID фильма должен быть положительным!")
    private Integer filmId;

    @JsonIgnore
    private Set<User> likes = new HashSet<>();

    @JsonIgnore
    private Set<User> dislikes = new HashSet<>();

    private int useful;

    @JsonIgnore
    public void addLike(User user) {
        log.debug("Пользователь {} поставил лайк отзыву {}", user.getId(), reviewId);
        likes.add(user);
        dislikes.remove(user);
        recalculateUseful();
    }

    @JsonIgnore
    public void addDislike(User user) {
        log.debug("Пользователь {} поставил дизлайк отзыву {}", user.getId(), reviewId);
        dislikes.add(user);
        likes.remove(user);
        recalculateUseful();
    }

    @JsonIgnore
    public void removeReaction(User user) {
        log.debug("Пользователь {} удалил реакцию с отзыва {}", user.getId(), reviewId);
        likes.remove(user);
        dislikes.remove(user);
        recalculateUseful();
    }

    @JsonIgnore
    private void recalculateUseful() {
        this.useful = likes.size() - dislikes.size();
        log.debug("Новый рейтинг отзыва {}: {}", reviewId, useful);
    }
}