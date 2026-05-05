package ru.yandex.practicum.filmorate.storage.like;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.DbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmEnricher;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class LikeDbStorage extends DbStorage<Film> implements LikeStorage {
    private static final String ADD_LIKE_QUERY = "MERGE INTO likes (film_id, user_id)" +
            " KEY (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String GET_POPULAR_FILMS_QUERY_BASE = "SELECT f.*, l.count, r.name AS rating_name FROM films f " +
            "LEFT JOIN (SELECT film_id, COUNT(user_id) AS count FROM likes GROUP BY film_id) l USING (film_id) " +
            "JOIN ratings r USING (rating_id) ";

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FilmEnricher filmEnricher;

    public LikeDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, @Qualifier("userDbStorage") UserStorage userStorage,
                         @Qualifier("filmDbStorage") FilmStorage filmStorage, FilmEnricher filmEnricher) {
        super(jdbc, mapper);
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
        this.filmEnricher = filmEnricher;
    }

    public boolean likeFilm(int filmId, int userId) {
        userStorage.checkUserExistence(userId);
        filmStorage.checkFilmExistence(filmId);

        try {
            simpleInsert(ADD_LIKE_QUERY, filmId, userId);
            return true;
        } catch (DuplicateKeyException e) {
            log.debug("Лайк от пользователя {} к фильму {} уже существует", userId, filmId);
            return false;
        }
    }

    public void deleteLike(int filmId, int userId) {
        userStorage.checkUserExistence(userId);
        filmStorage.checkFilmExistence(filmId);

        delete(DELETE_LIKE_QUERY, filmId, userId);
    }

    @Override
    public Collection<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        StringBuilder query = new StringBuilder(GET_POPULAR_FILMS_QUERY_BASE);
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (genreId != null) {
            query.append("JOIN films_genres fg ON fg.film_id = f.film_id ");
            conditions.add("fg.genre_id = ?");
            params.add(genreId);
        }

        if (year != null) {
            conditions.add("EXTRACT(YEAR FROM f.release_date) = ?");
            params.add(year);
        }

        if (!conditions.isEmpty()) {
            query.append("WHERE ").append(String.join(" AND ", conditions)).append(" ");
        }

        query.append("ORDER BY COALESCE(l.count, 0) DESC, f.film_id ASC LIMIT ?");
        params.add(count);

        Collection<Film> films = findMany(query.toString(), params.toArray());
        return filmEnricher.enrichFilms(films);
    }
}
