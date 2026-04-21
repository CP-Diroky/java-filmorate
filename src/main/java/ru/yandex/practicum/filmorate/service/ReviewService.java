package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage,
                         @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        this.reviewStorage = reviewStorage;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public Review newAddReview(Review review) {
        Long id = review.getUserId();
        if (id == null) {
            throw new NotFoundException("Пользователь должен существовать");
        }
        userStorage.getUserById(id);
        filmStorage.getFilmById(review.getFilmId());
        return reviewStorage.addReview(review);
    }


    public Review updateReview(Review review) {
        allValidateReviewUserFilm(review.getReviewId(), review.getUserId(), review.getFilmId());
        return reviewStorage.updateReview(review);
    }

    public void deleteReviewById(Long id) {
        getReviewById(id);
        reviewStorage.deleteReview(id);
    }

    public Review getReviewById(Long id) {
        return reviewStorage.getReview(id);
    }

    public Collection<Review> getAllReviewsCount(Long filmId, int count) {
        if (filmId == null) {
            return reviewStorage.getAllReviews(count);
        }
        filmStorage.getFilmById(filmId);
        return reviewStorage.getReviewsByFilmId(filmId, count);
    }

    public Review putLikeReview(Long id, Long userId) {
        validateReviewUser(id, userId);
        return reviewStorage.addLike(id, userId);
    }

    public Review putDislikeReview(Long id, Long userId) {
        validateReviewUser(id, userId);
        return reviewStorage.addDislike(id, userId);
    }

    public Review deleteLikeOnReview(Long id, Long userId) {
        validateReviewUser(id, userId);
        return reviewStorage.deleteLike(id, userId);
    }

    public Review deleteDislikeOnReview(Long id, Long userId) {
        validateReviewUser(id, userId);
        return reviewStorage.deleteDislike(id, userId);
    }

    private void allValidateReviewUserFilm(Long reviewId, Long userId, Long filmId) {
        reviewStorage.getReview(reviewId);
        userStorage.getUserById(userId);
        filmStorage.getFilmById(filmId);
    }

    private void validateReviewUser(Long reviewId, Long userId) {
        reviewStorage.getReview(reviewId);
        userStorage.getUserById(userId);
    }
}
