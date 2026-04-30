package ru.yandex.practicum.filmorate.storage.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DatabaseException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.DbStorage;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
public class ReviewDbStorage extends DbStorage<Review> implements ReviewStorage {
    private static final String ADD_REVIEW_QUERY =
            "INSERT INTO reviews(content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_REVIEW_QUERY =
            "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";

    private static final String DELETE_REVIEW_QUERY = "DELETE FROM reviews WHERE review_id = ?";

    private static final String GET_REVIEW_BY_ID_QUERY = "SELECT * FROM reviews WHERE review_id = ?";

    private static final String GET_REVIEWS_BY_FILM_QUERY =
            "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC, review_id ASC LIMIT ?";

    private static final String ADD_REACTION_QUERY =
            "MERGE INTO review_likes(review_id, user_id, is_like) KEY(review_id, user_id) VALUES (?, ?, ?)";

    private static final String REMOVE_REACTION_QUERY =
            "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";

    private static final String UPDATE_USEFUL_QUERY =
            "UPDATE reviews SET useful = (SELECT COALESCE(SUM(CASE WHEN is_like THEN 1 ELSE -1 END), 0) " +
                    "FROM review_likes WHERE review_id = ?) WHERE review_id = ?";

    private static final String GET_ALL_REVIEWS_QUERY =
            "SELECT * FROM reviews ORDER BY useful DESC, review_id ASC LIMIT ?";

    public ReviewDbStorage(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Review addReview(Review review) {
        Optional<Integer> id = insert(ADD_REVIEW_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                0); // новый отзыв начинается с useful = 0
        if (id.isPresent()) {
            review.setReviewId(id.get());
            log.debug("Добавлен отзыв с id {}", review.getReviewId());
            return review;
        } else {
            throw new DatabaseException("Не удалось добавить отзыв");
        }
    }

    @Override
    public Review updateReview(Review review) {
        checkReviewId(review.getReviewId());
        int rows = jdbc.update(UPDATE_REVIEW_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
        if (rows == 0) {
            throw new DatabaseException("Не удалось обновить отзыв");
        }
        log.debug("Обновлён отзыв с id {}", review.getReviewId());
        return getReviewById(review.getReviewId()).orElseThrow();
    }

    @Override
    public void deleteReview(int reviewId) {
        checkReviewId(reviewId);
        delete(DELETE_REVIEW_QUERY, reviewId);
        log.debug("Удалён отзыв с id {}", reviewId);
    }

    @Override
    public Optional<Review> getReviewById(int reviewId) {
        return findOne(GET_REVIEW_BY_ID_QUERY, reviewId);
    }

    @Override
    public Collection<Review> getReviewsByFilmId(int filmId, int count) {
        log.debug("Получаем до {} отзывов для фильма {}", count, filmId);
        if (filmId <= 0) {
            // Если filmId не указан или 0 — возвращаем все отзывы
            return findMany(GET_ALL_REVIEWS_QUERY, count);
        }
        return findMany(GET_REVIEWS_BY_FILM_QUERY, filmId, count);
    }

    @Override
    public void addReaction(int reviewId, int userId, boolean isLike) {
        checkReviewId(reviewId);
        simpleInsert(ADD_REACTION_QUERY, reviewId, userId, isLike);
        updateUsefulRating(reviewId);
        log.debug("Пользователь {} добавил реакцию (like={}) к отзыву {}", userId, isLike, reviewId);
    }

    @Override
    public void removeReaction(int reviewId, int userId) {
        checkReviewId(reviewId);
        delete(REMOVE_REACTION_QUERY, reviewId, userId);
        updateUsefulRating(reviewId);
        log.debug("Пользователь {} удалил реакцию с отзыва {}", userId, reviewId);
    }

    @Override
    public void updateUsefulRating(int reviewId) {
        jdbc.update(UPDATE_USEFUL_QUERY, reviewId, reviewId);
    }

    private void checkReviewId(int reviewId) {
        if (getReviewById(reviewId).isEmpty()) {
            throw new NotFoundException("Отзыв с id " + reviewId + " не найден");
        }
    }
}