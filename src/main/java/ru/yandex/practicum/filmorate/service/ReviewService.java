package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.OperationType;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.Instant;
import java.util.Collection;

@Slf4j
@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final EventStorage eventStorage;

    public ReviewService(
            ReviewStorage reviewStorage,
            @Qualifier("userDbStorage") UserStorage userStorage,
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            EventStorage eventStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.eventStorage = eventStorage;
    }

    public Review addReview(Review review) {
        validateReview(review);
        // Проверяем существование пользователя и фильма
        userStorage.getUserById(review.getUserId());
        filmStorage.getFilmById(review.getFilmId());

        log.debug("Создаём отзыв для фильма {} от пользователя {}", review.getFilmId(), review.getUserId());
        Review result = reviewStorage.addReview(review);

        Event event = Event.builder()
                .userId(result.getUserId())
                .timestamp(Instant.now().toEpochMilli())
                .eventType(EventType.REVIEW)
                .operation(OperationType.ADD)
                .entityId(result.getReviewId())
                .build();
        eventStorage.addEvent(event);
        return result;
    }

    public Review updateReview(Review review) {
        validateReview(review);
        if (review.getReviewId() == 0) {
            throw new ValidateException("Необходимо указать id отзыва для обновления!");
        }
        log.debug("Обновляем отзыв с id {}", review.getReviewId());
        Review result = reviewStorage.updateReview(review);

        Event event = Event.builder()
                .userId(result.getUserId())
                .timestamp(Instant.now().toEpochMilli())
                .eventType(EventType.REVIEW)
                .operation(OperationType.UPDATE)
                .entityId(result.getReviewId())
                .build();
        eventStorage.addEvent(event);
        return result;
    }

    public void deleteReview(int reviewId) {
        log.debug("Удаляем отзыв с id {}", reviewId);
        Review result = getReviewById(reviewId);
        reviewStorage.deleteReview(reviewId);

        Event event = Event.builder()
                .userId(result.getUserId())
                .timestamp(Instant.now().toEpochMilli())
                .eventType(EventType.REVIEW)
                .operation(OperationType.REMOVE)
                .entityId(reviewId)
                .build();
        eventStorage.addEvent(event);
    }

    public Review getReviewById(int reviewId) {
        return reviewStorage.getReviewById(reviewId)
                .orElseThrow(() -> {
                    log.debug("Отзыв с id {} не найден", reviewId);
                    return new NotFoundException("Отзыв с id " + reviewId + " не найден");
                });
    }

    public Collection<Review> getReviews(Integer filmId, int count) {
        if (count <= 0) {
            throw new ValidateException("Параметр count должен быть положительным!");
        }
        if (filmId != null) {
            if (filmId <= 0) {
                throw new ValidateException("ID фильма должен быть положительным!");
            }
            filmStorage.getFilmById(filmId); // Проверка существования
        }
        log.debug("Получаем отзывы: filmId={}, count={}", filmId, count);
        return reviewStorage.getReviewsByFilmId(filmId, count);
    }

    public void likeReview(int reviewId, int userId) {
        userStorage.getUserById(userId);
        validateIds(reviewId, userId);
        reviewStorage.addReaction(reviewId, userId, true);
        log.debug("Пользователь {} лайкнул отзыв {}", userId, reviewId);
    }

    public void dislikeReview(int reviewId, int userId) {
        validateIds(reviewId, userId);
        userStorage.getUserById(userId);
        reviewStorage.addReaction(reviewId, userId, false);
        log.debug("Пользователь {} дизлайкнул отзыв {}", userId, reviewId);
    }

    public void removeReaction(int reviewId, int userId) {
        validateIds(reviewId, userId);
        userStorage.getUserById(userId);
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