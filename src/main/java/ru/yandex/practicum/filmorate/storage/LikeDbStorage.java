package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

@Slf4j
@Repository
public class LikeDbStorage extends DbStorage<Film> implements LikeStorage {
    private static final String ADD_LIKE_QUERY = "INSERT INTO likes(film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String GET_POPULAR_FILMS_QUERY = "SELECT f.*, l.count, r.name AS rating_name FROM films f " +
            "JOIN (SELECT film_id, COUNT(user_id) AS count FROM likes GROUP BY film_id ORDER BY count DESC LIMIT ?) l " +
            "USING (film_id) " +
            "JOIN ratings r USING (rating_id) " +
            "ORDER BY l.count DESC";

    public LikeDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    public void likeFilm(int filmId, int userId) {
        simpleInsert(ADD_LIKE_QUERY, filmId, userId);
    }

    public void deleteLike(int filmId, int userId) {
        delete(DELETE_LIKE_QUERY, filmId, userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        return findMany(GET_POPULAR_FILMS_QUERY, count);
    }
}
