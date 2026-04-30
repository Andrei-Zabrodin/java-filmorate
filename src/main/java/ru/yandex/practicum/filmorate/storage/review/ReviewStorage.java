package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;
import java.util.Collection;
import java.util.Optional;

public interface ReviewStorage {
    Review addReview(Review review);

    Review updateReview(Review review);

    void deleteReview(int reviewId);

    Optional<Review> getReviewById(int reviewId);

    Collection<Review> getReviewsByFilmId(int filmId, int count);

    void addReaction(int reviewId, int userId, boolean isLike);

    void removeReaction(int reviewId, int userId);

    void updateUsefulRating(int reviewId);
}
