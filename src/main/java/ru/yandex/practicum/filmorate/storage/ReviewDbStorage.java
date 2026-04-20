package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Repository
@Slf4j
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final String CREATE_REVIEW =
            "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_REVIEW =
            "UPDATE reviews SET content = ?, is_positive = ?, user_id = ?, film_id = ? WHERE id = ?";

    private static final String DELETE_REVIEW =
            "DELETE FROM reviews WHERE id = ?";

    private static final String GET_REVIEW_BY_ID =
            "SELECT * FROM reviews WHERE id = ?";

    private static final String GET_ALL_REVIEWS =
            "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";

    private static final String GET_REVIEWS_BY_FILM_ID =
            "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Review addReview(Review review) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE_REVIEW, new String[]{"id"});
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            ps.setLong(5, 0L);
            return ps;
        }, keyHolder);

        review.setReviewId(keyHolder.getKey().longValue());
        review.setUseful(0L);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        Long reviewId = review.getReviewId();

        jdbcTemplate.update(UPDATE_REVIEW,
                review.getContent(),
                review.getPositive(),
                review.getUserId(),
                review.getFilmId(),
                reviewId);

        return getReview(reviewId);
    }

    @Override
    public void deleteReview(Long id) {
        jdbcTemplate.update(DELETE_REVIEW, id);
    }

    @Override
    public Review getReview(Long id) {
        try {
            return jdbcTemplate.queryForObject(GET_REVIEW_BY_ID, this::mapRowToReview, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Отзыв не найден");
        }
    }

    @Override
    public Collection<Review> getAllReviews(int count) {
        return jdbcTemplate.query(GET_ALL_REVIEWS, this::mapRowToReview, count);
    }

    @Override
    public Collection<Review> getReviewsByFilmId(Long filmId, int count) {
        return jdbcTemplate.query(GET_REVIEWS_BY_FILM_ID, this::mapRowToReview, filmId, count);
    }

    @Override
    public Review addLike(Long reviewId, Long userId) {
        String sql = "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?";
        Boolean currentReaction;
        try {
            currentReaction = jdbcTemplate.queryForObject(sql, Boolean.class, reviewId, userId);
        } catch (EmptyResultDataAccessException e) {
            currentReaction = null;
        }

        if (currentReaction == null) {
            String insertLike = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, true)";
            String updUseful = "UPDATE reviews SET useful = useful + 1 WHERE id = ?";
            jdbcTemplate.update(insertLike, reviewId, userId);
            jdbcTemplate.update(updUseful, reviewId);
        } else if (!currentReaction) {
            String updateReaction = "UPDATE review_likes SET is_like = true WHERE review_id = ? AND user_id = ?";
            String incUsefulByTwo = "UPDATE reviews SET useful = useful + 2 WHERE id = ?";
            jdbcTemplate.update(updateReaction, reviewId, userId);
            jdbcTemplate.update(incUsefulByTwo, reviewId);
        }

        return getReview(reviewId);
    }

    @Override
    public Review addDislike(Long reviewId, Long userId) {
        String sql = "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?";
        Boolean currentReaction;
        try {
            currentReaction = jdbcTemplate.queryForObject(sql, Boolean.class, reviewId, userId);
        } catch (EmptyResultDataAccessException e) {
            currentReaction = null;
        }

        if (currentReaction == null) {
            String insertDislike = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, false)";
            jdbcTemplate.update(insertDislike, reviewId, userId);
            String updUseful = "UPDATE reviews SET useful = useful - 1 WHERE id = ?";
            jdbcTemplate.update(updUseful, reviewId);
        } else if (currentReaction) {
            String putDislike = "UPDATE review_likes SET is_like = false  WHERE review_id = ? AND user_id = ?";
            jdbcTemplate.update(putDislike, reviewId, userId);
            String updUseful = "UPDATE reviews SET useful = useful - 2 WHERE id = ?";
            jdbcTemplate.update(updUseful, reviewId);
        }
        return getReview(reviewId);
    }

    @Override
    public Review deleteLike(Long reviewId, Long userId) {
        String sql = "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?";
        Boolean currentReaction;
        try {
            currentReaction = jdbcTemplate.queryForObject(sql, Boolean.class, reviewId, userId);
        } catch (EmptyResultDataAccessException e) {
            currentReaction = null;
        }

        if (Boolean.TRUE.equals(currentReaction)) {
            String removeLike = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
            String updUseful = "UPDATE reviews SET useful = useful - 1 WHERE id = ?";
            jdbcTemplate.update(removeLike, reviewId, userId);
            jdbcTemplate.update(updUseful, reviewId);
        }

        return getReview(reviewId);
    }

    @Override
    public Review deleteDislike(Long reviewId, Long userId) {
        String sql = "SELECT is_like FROM review_likes WHERE review_id = ? AND user_id = ?";
        Boolean currentReaction;
        try {
            currentReaction = jdbcTemplate.queryForObject(sql, Boolean.class, reviewId, userId);
        } catch (EmptyResultDataAccessException e) {
            currentReaction = null;
        }

        if (Boolean.FALSE.equals(currentReaction)) {
            String removeDislike = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
            String updUseful = "UPDATE reviews SET useful = useful + 1 WHERE id = ?";
            jdbcTemplate.update(removeDislike, reviewId, userId);
            jdbcTemplate.update(updUseful, reviewId);
        }

        return getReview(reviewId);
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getLong("id"));
        review.setContent(rs.getString("content"));
        review.setPositive(rs.getBoolean("is_positive"));
        review.setUserId(rs.getLong("user_id"));
        review.setFilmId(rs.getLong("film_id"));
        review.setUseful(rs.getLong("useful"));
        return review;
    }
}
