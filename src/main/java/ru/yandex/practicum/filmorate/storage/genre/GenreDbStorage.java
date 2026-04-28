package ru.yandex.practicum.filmorate.storage.genre;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.DbStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class GenreDbStorage extends DbStorage<Genre> implements GenreStorage {
    private static final String GET_GENRES_QUERY = "SELECT * FROM genres";
    private static final String GET_GENRE_BY_ID_QUERY = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String GET_GENRES_BY_FILM_QUERY = "SELECT fg.genre_id, g.name FROM films_genres fg " +
            "JOIN genres g USING(genre_id) WHERE film_id = ?";
    private static final String GET_GENRES_BY_FILMS_QUERY = "SELECT * FROM films_genres JOIN genres USING (genre_id)";
    private static final String ADD_FILM_GENRES_QUERY = "INSERT INTO films_genres (film_id, genre_id) " +
            "VALUES (?, ?)";

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Genre> getGenres() {
        return findMany(GET_GENRES_QUERY);
    }

    @Override
    public Genre getGenreById(int id) {
        Optional<Genre> genre = findOne(GET_GENRE_BY_ID_QUERY, id);
        if (genre.isPresent()) {
            log.debug("Нашли жанр с id {}", id);
            return genre.get();
        } else {
            log.debug("Не удалось найти жанр с указанным id");
            throw new NotFoundException("Жанр с id " + id + " не найден");
        }
    }

    @Override
    public Set<Genre> getGenresForOneFilm(int filmId) {
        return new HashSet<>(findMany(GET_GENRES_BY_FILM_QUERY, filmId));
    }

    @Override
    public Map<Integer, Set<Genre>> getGenresForAllFilms(List<Integer> filmIds) {
        Map<Integer, Set<Genre>> result = new HashMap<>();

        jdbc.query(GET_GENRES_BY_FILMS_QUERY,
                rs -> {
                    Integer filmId = rs.getInt("film_id");
                    Genre genre = new Genre();
                    genre.setId(rs.getInt("genre_id"));
                    genre.setName(rs.getString("name"));
                    result.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
                });

        return result;
    }

    @Override
    public void addFilmGenres(int filmId, Set<Integer> genreIds) {
        if (genreIds.isEmpty()) {
            return;
        }

        List<Object[]> batch = genreIds.stream()
                .peek(this::getGenreById)
                .map(genreId -> new Object[]{filmId, genreId})
                .collect(Collectors.toList());

        jdbc.batchUpdate(ADD_FILM_GENRES_QUERY, batch);
        log.debug("Добавили к фильму с id {} количество жанров: {}", filmId, genreIds.size());
    }

}