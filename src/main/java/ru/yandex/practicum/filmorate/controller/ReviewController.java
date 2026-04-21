package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

@RestController
@RequestMapping("/reviews")
@Validated
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review addReview(@RequestBody @Valid Review review) {
        return reviewService.newAddReview(review);
    }

    @PutMapping
    public Review updateReview(@RequestBody @Valid Review review) {
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable @Positive Long id) {
        reviewService.deleteReviewById(id);
    }

    @GetMapping("/{id}")
    public Review getReviewById(@PathVariable @Positive Long id) {
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public Collection<Review> getReviews(@RequestParam(required = false) Long filmId,
                                         @RequestParam(defaultValue = "10") int count) {
        return reviewService.getAllReviewsCount(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public Review putLikeReview(@PathVariable @Positive Long id,
                                @PathVariable @Positive Long userId) {
        return reviewService.putLikeReview(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public Review putDislikeReview(@PathVariable @Positive Long id,
                                   @PathVariable @Positive Long userId) {
        return reviewService.putDislikeReview(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Review deleteLikeOnReview(@PathVariable @Positive Long id,
                                     @PathVariable @Positive Long userId) {
        return reviewService.deleteLikeOnReview(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public Review deleteDislikeOnReview(@PathVariable @Positive Long id,
                                        @PathVariable @Positive Long userId) {
        return reviewService.deleteDislikeOnReview(id, userId);
    }

}
