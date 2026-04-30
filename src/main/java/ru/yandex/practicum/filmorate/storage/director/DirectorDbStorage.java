package ru.yandex.practicum.filmorate.storage.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DatabaseException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DbStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class DirectorDbStorage extends DbStorage<Director> implements DirectorStorage {
    private static final String GET_DIRECTORS_QUERY = "SELECT * FROM directors";
    private static final String GET_DIRECTOR_BY_ID_QUERY = "SELECT * FROM directors WHERE director_id = ?";
    private static final String GET_DIRECTORS_BY_FILM_QUERY = "SELECT fd.film_id, d.director_id, d.name FROM films_directors fd " +
            "JOIN directors d USING(director_id) WHERE fd.film_id = ?";
    private static final String GET_DIRECTORS_BASE_QUERY = "SELECT fd.film_id, d.director_id, d.name FROM films_directors fd " +
            "JOIN directors d USING(director_id)";
    private static final String ADD_FILM_DIRECTORS_QUERY = "INSERT INTO films_directors (film_id, director_id) VALUES (?, ?)";
    private static final String ADD_DIRECTOR_QUERY = "INSERT INTO directors(name) VALUES (?)";
    private static final String UPDATE_DIRECTOR_QUERY = "UPDATE directors SET name = ? WHERE director_id = ?";
    private static final String DELETE_DIRECTOR_QUERY = "DELETE FROM directors WHERE director_id = ?";

    public DirectorDbStorage(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Director> getDirectors() {
        return findMany(GET_DIRECTORS_QUERY);
    }

    @Override
    public Optional<Director> getDirectorById(int id) {
        return findOne(GET_DIRECTOR_BY_ID_QUERY, id);
    }

    @Override
    public Director addDirector(Director director) {
        Integer id = insert(ADD_DIRECTOR_QUERY, director.getName())
                .orElseThrow(() -> new DatabaseException("Не удалось добавить данные"));

        director.setId(id);
        log.debug("Добавлен режиссёр с id {}", id);

        return director;
    }

    @Override
    public void updateDirector(Director director) {
        update(UPDATE_DIRECTOR_QUERY, director.getName(), director.getId());
    }

    @Override
    public void deleteDirector(int id) {
        delete(DELETE_DIRECTOR_QUERY, id);
    }

    public Set<Director> getDirectorsForOneFilm(int filmId) {
        return new HashSet<>(findMany(GET_DIRECTORS_BY_FILM_QUERY, filmId));
    }

    public Map<Integer, Set<Director>> getDirectorsForAllFilms(List<Integer> filmIds) {
        Map<Integer, Set<Director>> result = new HashMap<>();

        if (filmIds == null || filmIds.isEmpty()) {
            return result;
        }

        String placeholders = filmIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(", "));

        String sql = GET_DIRECTORS_BASE_QUERY +
                " WHERE fd.film_id IN (" + placeholders + ")";

        query(sql, filmIds.toArray(), rs -> {
            Integer filmId = rs.getInt("film_id");

            Director director = new Director();
            director.setId(rs.getInt("director_id"));
            director.setName(rs.getString("name"));

            result.computeIfAbsent(filmId, k -> new HashSet<>()).add(director);
        });

        return result;
    }

    public void addFilmDirectors(int filmId, Set<Integer> directorIds) {
        if (directorIds == null || directorIds.isEmpty()) {
            return;
        }

        List<Object[]> batch = directorIds.stream()
                .peek(this::getDirectorById)
                .map(directorId -> new Object[]{filmId, directorId})
                .collect(Collectors.toList());

        batchUpdate(ADD_FILM_DIRECTORS_QUERY, batch);

        log.debug("Добавили к фильму с id {} количество режиссёров: {}", filmId, directorIds.size());
    }
}
