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
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
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
        validateDirectors(film.getDirectors());

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

        //Добавляем режиссёров
        saveFilmDirectors(film.getId(), film.getDirectors());

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
        validateDirectors(film.getDirectors());

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

        // удалить старых режиссёров
        jdbcTemplate.update("DELETE FROM film_directors WHERE film_id = ?", film.getId());

        // добавить новых
        saveFilmDirectors(film.getId(), film.getDirectors());

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
    public List<Film> getPopularFilms(int count, Long genreId, Integer year) {
        String sql = """
                        SELECT f.*
                        FROM films f
                        LEFT JOIN film_likes fl ON f.id = fl.film_id
                WHERE 1=1
                """
                + (genreId != null ? " AND EXISTS (SELECT 1 FROM film_genres fg " +
                "WHERE fg.film_id = f.id AND fg.genre_id = ?) " : "")
                + (year != null ? " AND YEAR(f.release_date) = ? " : "")
                + """
                        GROUP BY f.id
                        ORDER BY COUNT(fl.user_id) DESC
                        LIMIT ?
                """;

        List<Object> params = new ArrayList<>();
        if (genreId != null) params.add(genreId);
        if (year != null) params.add(year);
        params.add(count);

        return jdbcTemplate.query(sql, this::mapRowToFilm, params.toArray());
    }

    @Override
    public Collection<Film> getSortDirectorsFilmsByLikes(Long directorId) {
        directorExist(directorId);

        String sql = """
                SELECT f.*,
                COUNT(fl.user_id) AS likes_count
                FROM films f
                JOIN film_directors fd ON f.id = fd.film_id
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                WHERE fd.director_id = ?
                GROUP BY f.id
                ORDER BY likes_count DESC
                """;
        return jdbcTemplate.query(sql, this::mapRowToFilm, directorId);
    }

    @Override
    public Collection<Film> getSortDirectorsFilmsByYear(Long directorId) {
        directorExist(directorId);
        String sql = """
                SELECT f.*
                FROM films f
                JOIN film_directors fd ON f.id = fd.film_id
                WHERE fd.director_id = ?
                GROUP BY f.id
                ORDER BY f.release_date
                """;

        return jdbcTemplate.query(sql, this::mapRowToFilm, directorId);
    }

    @Override
    public Collection<Film> getSearchedFilmsByTitle(String query) {
        String sql = """
                SELECT *
                FROM films
                WHERE LOWER(name) LIKE ?
                """;
        return jdbcTemplate.query(sql, this::mapRowToFilm, "%" + query.toLowerCase() + "%");
    }

    @Override
    public Collection<Film> getSearchedFilmsByDirector(String query) {
        String sql = """
                SELECT f.*
                FROM films f
                JOIN film_directors fd ON f.id = fd.film_id
                JOIN directors d ON fd.director_id = d.id
                WHERE LOWER(d.name) LIKE ?
                """;
        return jdbcTemplate.query(sql, this::mapRowToFilm, "%" + query.toLowerCase() + "%");
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
        film.setDirectors(getDirectors(film.getId()));
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

    private Set<Director> getDirectors(Long filmId) {
        String sql = """
                    SELECT d.*
                    FROM directors d
                    JOIN film_directors fd ON d.id = fd.director_id
                    WHERE fd.film_id = ?
                """;

        return new HashSet<>(jdbcTemplate.query(sql, (rs, rowNum) -> {
            Director director = new Director();
            director.setId(rs.getLong("id"));
            director.setName(rs.getString("name"));
            return director;
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

    //Проверка режиссёров

    private void validateDirectors(Set<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return;
        }

        Set<Long> genreIds = directors.stream()
                .map(Director::getId)
                .collect(Collectors.toSet());

        String placeholders = genreIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sql = "SELECT id FROM directors WHERE id IN (" + placeholders + ")";

        List<Long> existingIds = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> rs.getLong("id"),
                genreIds.toArray()
        );

        if (existingIds.size() != genreIds.size()) {
            throw new NotFoundException("Режиссёр не найден");
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

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        String sql = """
                SELECT f.*
                FROM films f
                WHERE f.id IN (
                    SELECT fl1.film_id
                    FROM film_likes fl1
                    WHERE fl1.user_id = ?
                    INTERSECT
                    SELECT fl2.film_id
                    FROM film_likes fl2
                    WHERE fl2.user_id = ?
                )
                ORDER BY (
                    SELECT COUNT(*)
                    FROM film_likes fl
                    WHERE fl.film_id = f.id
                ) DESC
                """;

        return jdbcTemplate.query(sql, this::mapRowToFilm, userId, friendId);
    }

    private void saveFilmDirectors(Long filmId, Set<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";

        jdbcTemplate.batchUpdate(sql, directors, directors.size(),
                (ps, director) -> {
                    ps.setLong(1, filmId);
                    ps.setLong(2, director.getId());
                });
    }

    private boolean directorExist(Long directorId) {
        String sql = "SELECT COUNT(*) as cnt FROM directors WHERE id = ?";

        try {
            int exist = jdbcTemplate.queryForObject(
                    sql,
                    (rs, rowNum) -> rs.getInt("cnt"),
                    directorId
            );

            if (exist == 1) {
                return true;
            }
        } catch (NullPointerException ignore) {
            throw new NotFoundException("Режиссёр не найден");
        }

        throw new NotFoundException("Режиссёр не найден");
    }
}
