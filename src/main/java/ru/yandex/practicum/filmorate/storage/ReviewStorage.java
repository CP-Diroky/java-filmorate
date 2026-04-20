package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

public interface ReviewStorage {
    Review addReview(Review review);
    Review updateReview(Review review);
    void deleteReview(Long id);
    Review getReview(Long id);
    Collection<Review> getAllReviews(int count);
    Collection<Review> getReviewsByFilmId(Long filmId, int count);
    Review addLike(Long reviewId, Long userId);
    Review addDislike(Long reviewId, Long userId);
    Review deleteLike(Long reviewId, Long userId);
    Review deleteDislike(Long reviewId, Long userId);
}
