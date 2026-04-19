package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

@Repository
@Qualifier("directorDbStorage")
public class DirectorsDbStorage implements DirectorsStorage {

    private final JdbcTemplate jdbcTemplate;

    public DirectorsDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Director> getAllDirectors() {
        String sql = "SELECT * FROM directors ORDER BY id";
        return jdbcTemplate.query(sql, this::mapRowToDirector);
    }

    @Override
    public Director getDirectorById(Long id) {
        String sql = "SELECT * FROM directors WHERE id = ?";
        List<Director> directors = jdbcTemplate.query(sql, this::mapRowToDirector, id);
        if (directors.isEmpty()) {
            throw new NotFoundException("Режиссёр не найден");
        }
        return directors.getFirst();
    }

    @Override
    public void deleteDirector(Long id) {
        String sql1 = "DELETE FROM film_directors WHERE director_id = ?";
        jdbcTemplate.update(sql1, id);

        String sql2 = "DELETE FROM directors WHERE id = ?";
        jdbcTemplate.update(sql2, id);
    }

    @Override
    public Director addDirector(Director director) {
        String sql = "INSERT INTO directors(name) VALUES (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setId(keyHolder.getKey().longValue());

        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        String sql = "UPDATE directors SET name=? WHERE id=?";

        int rows = jdbcTemplate.update(sql, director.getName(), director.getId());

        if (rows == 0) {
            throw new NotFoundException("Режиссёр не найден");
        }

        return director;
    }

    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        Director director = new Director();
        director.setId(rs.getLong("id"));
        director.setName(rs.getString("name"));
        return director;
    }
}
