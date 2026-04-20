package ru.yandex.practicum.filmorate.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final Logger log = LoggerFactory.getLogger(FilmDbStorage.class);

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Film> getAllFilms() {
        String sql = "SELECT * FROM films";
        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    @Override
    public Film getFilmById(Long id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, this::mapRowToFilm, id);
        } catch (RuntimeException e) { //Будем ловить исключения и передавать их как 404
            throw new NotFoundException("Фильм не найден");
        }
    }

    @Override
    public Film addFilm(Film film) {
        String errorMessage;
        //Проверка
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            errorMessage = "Дата выпуска не должна быть раньше 28 декабря 1895 года!";
            log.error(errorMessage);
            throw new ConditionsNotMetException(errorMessage);
        }

        if (film.getMpa() == null || film.getMpa().getId() == null) {
            errorMessage = "Рейтинг фильма должен быть указан!";
            log.error(errorMessage);
            throw new ConditionsNotMetException(errorMessage);
        }

        validateGenres(film.getGenres());
        validateMpa(film.getMpa().getId());

        String sql = "INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());

        //Добавляем жанры
        saveFilmGenres(film.getId(), film.getGenres());

        return film;
    }

    @Override
    public Film changeFilm(Film film) {
        String errorMessage;
        //Проверка
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            errorMessage = "Дата выпуска не должна быть раньше 28 декабря 1895 года!";
            log.error(errorMessage);
            throw new ConditionsNotMetException(errorMessage);
        }

        if (film.getMpa() == null || film.getMpa().getId() == null) {
            errorMessage = "Рейтинг фильма должен быть указан!";
            log.error(errorMessage);
            throw new ConditionsNotMetException(errorMessage);
        }

        validateGenres(film.getGenres());
        validateMpa(film.getMpa().getId());

        String sql = "UPDATE films SET name=?, description=?, release_date=?, duration=?, mpa_id=? WHERE id=?";

        int rows = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (rows == 0) {
            throw new NotFoundException("Фильм не найден");
        }

        // удалить старые жанры
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());

        // добавить новые
        saveFilmGenres(film.getId(), film.getGenres());

        return film;
    }

    @Override
    public Film addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        return getFilmById(filmId);
    }

    @Override
    public Film deleteLike(Long filmId, Long userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
        return getFilmById(filmId);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        String sql = """
                SELECT f.*
                FROM films f
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                GROUP BY f.id
                ORDER BY COUNT(fl.user_id) DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, this::mapRowToFilm, count);
    }

    //Метод для получения рекомендаций
    @Override
    public Collection<Film> getRecommendation(Long userId) {
        //Проверяем есть ли записи в таблице film_likes
        String sql = "SELECT film_id FROM film_likes";
        List<Long> filmsIds = jdbcTemplate.queryForList(sql, Long.class);

        if (filmsIds.isEmpty()) throw new ConditionsNotMetException("Лайков нет!");

        sql = "SELECT film_id FROM film_likes WHERE user_id = ?"; //Получаем список фильмов с лайками от userId
        List<Long> filmsLikedByUserId = jdbcTemplate.queryForList(sql, Long.class, userId);

        int maxMatches = 0;
        int currentMatches = 0;
        Long otherIdWithMaxMatches = 0L;

        sql = "SELECT DISTINCT user_id FROM film_likes WHERE user_id <> ?";

        //Список остальных пользователей, которые поставили лайки
        List<Long> otherIds = jdbcTemplate.queryForList(sql, Long.class, userId);
        List<Long> filmsLikedByOtherId; //Список фильмов, которые получили лайки от другого пользователя

        sql = "SELECT film_id FROM film_likes WHERE user_id = ?";

        for (Long otherId : otherIds) {
            filmsLikedByOtherId = jdbcTemplate.queryForList(sql, Long.class, otherId);
            for (Long filmId : filmsLikedByOtherId) {
                if (filmsLikedByUserId.contains(filmId)) currentMatches++;
            }
            if (currentMatches > maxMatches) {
                maxMatches = currentMatches;
                otherIdWithMaxMatches = otherId;
            }
            currentMatches = 0;
        }

        if (otherIdWithMaxMatches.equals(0L)) return List.of(); //

        filmsLikedByOtherId = jdbcTemplate.queryForList(sql, Long.class, otherIdWithMaxMatches);

        return filmsLikedByOtherId.stream()
                .filter(filmId -> !(filmsLikedByUserId.contains(filmId)))
                .map(this::getFilmById).toList();


    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        Date releaseDate = rs.getDate("release_date");
        if (releaseDate != null) {
            film.setReleaseDate(releaseDate.toLocalDate());
        }

        film.setDuration(rs.getInt("duration"));

        Long mpaId = rs.getLong("mpa_id");

        String sql = "SELECT * FROM mpa WHERE id = ?";
        Mpa mpa = jdbcTemplate.queryForObject(sql, (mpaRs, i) -> {
            Mpa m = new Mpa();
            m.setId(mpaRs.getLong("id"));
            m.setName(mpaRs.getString("name"));
            return m;
        }, mpaId);

        film.setMpa(mpa);
        film.setGenres(getGenres(film.getId()));
        film.setUsersId(getLikesIds(film.getId()));
        return film;
    }

    private Set<Genre> getGenres(Long filmId) {
        String sql = """
                    SELECT g.*
                    FROM genres g
                    JOIN film_genres fg ON g.id = fg.genre_id
                    WHERE fg.film_id = ?
                """;

        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getLong("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, filmId));
    }

    private Set<Long> getLikesIds(Long filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";

        return new HashSet<>(jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("user_id"),
                filmId
        ));
    }

    //Проверка, что рейтинг существует

    private void validateMpa(Long mpaId) {
        String sql = "SELECT COUNT(*) FROM mpa WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, mpaId);
        if (count == null || count == 0) {
            throw new NotFoundException("Рейтинг не найден");
        }
    }

    //Проверка жанра

    private void validateGenres(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        Set<Long> genreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        String placeholders = genreIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sql = "SELECT id FROM genres WHERE id IN (" + placeholders + ")";

        List<Long> existingIds = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("id"),
                genreIds.toArray()
        );

        if (existingIds.size() != genreIds.size()) {
            throw new NotFoundException("Жанр не найден");
        }
    }


    private void saveFilmGenres(Long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, genres, genres.size(),
                (ps, genre) -> {
                    ps.setLong(1, filmId);
                    ps.setLong(2, genre.getId());
                });
    }
}
