package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import java.util.Collection;

@Slf4j
@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public ReviewService(
            ReviewStorage reviewStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Review addReview(Review review) {
        validateReview(review);
        // Проверяем существование пользователя и фильма
        userStorage.getUserById(review.getUserId());
        filmStorage.getFilmById(review.getFilmId());
        log.debug("Создаём отзыв для фильма {} от пользователя {}", review.getFilmId(), review.getUserId());
        return reviewStorage.addReview(review);
    }

    public Review updateReview(Review review) {
        validateReview(review);
        if (review.getReviewId() == 0) {
            throw new ValidateException("Необходимо указать id отзыва для обновления!");
        }
        log.debug("Обновляем отзыв с id {}", review.getReviewId());
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(int reviewId) {
        log.debug("Удаляем отзыв с id {}", reviewId);
        reviewStorage.deleteReview(reviewId);
    }

    public Review getReviewById(int reviewId) {
        return reviewStorage.getReviewById(reviewId)
                .orElseThrow(() -> {
                    log.debug("Отзыв с id {} не найден", reviewId);
                    return new ValidateException("Отзыв не найден");
                });
    }

    public Collection<Review> getReviews(int filmId, int count) {
        if (count <= 0) count = 10;
        log.debug("Получаем отзывы: filmId={}, count={}", filmId, count);
        return reviewStorage.getReviewsByFilmId(filmId, count);
    }

    public void likeReview(int reviewId, int userId) {
        validateIds(reviewId, userId);
        reviewStorage.addReaction(reviewId, userId, true);
        log.debug("Пользователь {} лайкнул отзыв {}", userId, reviewId);
    }

    public void dislikeReview(int reviewId, int userId) {
        validateIds(reviewId, userId);
        reviewStorage.addReaction(reviewId, userId, false);
        log.debug("Пользователь {} дизлайкнул отзыв {}", userId, reviewId);
    }

    public void removeReaction(int reviewId, int userId) {
        validateIds(reviewId, userId);
        reviewStorage.removeReaction(reviewId, userId);
        log.debug("Пользователь {} удалил реакцию с отзыва {}", userId, reviewId);
    }

    private void validateReview(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new ValidateException("Текст отзыва не может быть пустым!");
        }
        if (review.getIsPositive() == null) {
            throw new ValidateException("Тип отзыва должен быть указан!");
        }
    }

    private void validateIds(int reviewId, int userId) {
        if (reviewId <= 0 || userId <= 0) {
            throw new ValidateException("ID отзыва и пользователя должны быть положительными!");
        }
    }
}