package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DatabaseException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmSortBy;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.DbStorage;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.rating.RatingStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class FilmDbStorage extends DbStorage<Film> implements FilmStorage {
    private static final String GET_ALL_FILMS_QUERY = "SELECT f.*, r.name AS rating_name FROM films f " +
            "JOIN ratings r USING (rating_id)";
    private static final String GET_FILM_BY_ID_QUERY = "SELECT f.*, r.name AS rating_name FROM films f " +
            "JOIN ratings r USING (rating_id) WHERE f.film_id = ?";
    private static final String GET_FILMS_BY_DIRECTOR_SORT_BY_YEAR_QUERY = "SELECT f.*, r.name AS rating_name FROM films f " +
            "JOIN films_directors fd ON f.film_id = fd.film_id " +
            "JOIN ratings r USING (rating_id) " +
            " WHERE fd.director_id = ? " +
            " ORDER BY f.release_date";
    private static final String GET_FILMS_BY_DIRECTOR_SORT_BY_LIKES_QUERY = "SELECT f.*, l.count, r.name AS rating_name FROM films f " +
            "JOIN films_directors fd ON f.film_id = fd.film_id " +
            "JOIN ratings r USING (rating_id) " +
            "LEFT JOIN (SELECT film_id, COUNT(user_id) AS count FROM likes GROUP BY film_id) l USING (film_id) " +
            " WHERE fd.director_id = ? " +
            " ORDER BY count DESC, f.release_date";
    private static final String ADD_FILM_QUERY = "INSERT INTO films(name, description, release_date, duration," +
            " rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM_QUERY_START = "UPDATE films SET ";
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE film_id = ?";

    private final GenreDbStorage genreDbStorage;
    private final DirectorDbStorage directorDbStorage;
    private final RatingStorage ratingStorage;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, GenreDbStorage genreStorage,
                         DirectorDbStorage directorDbStorage,
                         RatingStorage ratingStorage) {
        super(jdbc, mapper);
        this.genreDbStorage = genreStorage;
        this.directorDbStorage = directorDbStorage;
        this.ratingStorage = ratingStorage;
    }

    @Override
    public Collection<Film> getFilms() {
        log.debug("Возвращаем список фильмов");
        Collection<Film> films = findMany(GET_ALL_FILMS_QUERY);
        return enrichFilms(films);
    }

    @Override
    public Film getFilmById(int id) {
        Optional<Film> filmOpt = findOne(GET_FILM_BY_ID_QUERY, id);
        if (filmOpt.isPresent()) {
            log.debug("Нашли фильм с id {}", id);
            return enrichFilm(filmOpt.get());
        } else {
            log.debug("Не удалось найти фильм с указанным id");
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
    }

    @Override
    public Film addFilm(Film film) {
        Set<Integer> genreIds = Optional.ofNullable(film.getGenres()).orElseGet(HashSet::new).stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());
        Set<Integer> directorIds = Optional.ofNullable(film.getDirectors()).orElseGet(HashSet::new).stream()
                .map(Director::getId)
                .collect(Collectors.toSet());
        int mpa = film.getMpa().getId();
        ratingStorage.getRatingById(mpa);

        Optional<Integer> id = insert(ADD_FILM_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                mpa);

        if (id.isPresent()) {
            film.setId(id.get());
            genreDbStorage.addFilmGenres(id.get(), genreIds);
            directorDbStorage.addFilmDirectors(id.get(), directorIds);

            log.debug("Добавлен фильм с id {}", film.getId());
            return film;
        } else {
            throw new DatabaseException("Не удалось добавить данные");
        }
    }

    @Override
    public Film updateFilm(Film newFilm) {
        checkFilmId(newFilm.getId());

        StringBuilder query = new StringBuilder(UPDATE_FILM_QUERY_START);
        List<Object> params = new ArrayList<>();

        if (newFilm.getName() != null) {
            query.append("name = ?, ");
            params.add(newFilm.getName());
        }

        if (newFilm.getDescription() != null) {
            query.append("description = ?, ");
            params.add(newFilm.getDescription());
        }

        if (newFilm.getReleaseDate() != null) {
            query.append("release_date = ?, ");
            params.add(newFilm.getReleaseDate());
        }

        if (newFilm.getDuration() != null) {
            query.append("duration = ?, ");
            params.add(newFilm.getDuration());
        }

        if (newFilm.getMpa() != null) {
            query.append("rating_id = ?, ");
            params.add(newFilm.getMpa().getId());
        }

        if (!params.isEmpty()) {
            query.setLength(query.length() - 2); //Убираем пробел и запятую с конца
            query.append(" WHERE film_id = ?");
            params.add(newFilm.getId());

            log.debug("Итоговые параметры для обновления: {}", params);
            update(query.toString(), params.toArray());
        }

        Film film = getFilmById(newFilm.getId());

        if (newFilm.getDirectors() != null && !newFilm.getDirectors().isEmpty()) {
            Set<Integer> directorIds = newFilm.getDirectors().stream()
                    .map(Director::getId)
                    .collect(Collectors.toSet());
            directorDbStorage.addFilmDirectors(newFilm.getId(), directorIds);
            film = getFilmById(newFilm.getId());
        }

        log.debug("Фильм с id {} обновлен", newFilm.getId());
        return film;
    }

    @Override
    public Film deleteFilm(int id) {
        checkFilmId(id);

        Film film = getFilmById(id);
        log.debug("Удаляем фильм с id {}", id);
        delete(DELETE_FILM_QUERY, id);

        return film;
    }

    private void checkFilmId(int filmId) {
        getFilmById(filmId);
    }

    @Override
    public Collection<Film> getFilmsByDirector(int directorId, FilmSortBy sortBy) {
        directorDbStorage.getDirectorById(directorId);

        Collection<Film> films = FilmSortBy.LIKES.equals(sortBy)
                ? findMany(GET_FILMS_BY_DIRECTOR_SORT_BY_LIKES_QUERY, directorId)
                : findMany(GET_FILMS_BY_DIRECTOR_SORT_BY_YEAR_QUERY, directorId);

        return enrichFilms(films);
    }

    public Collection<Film> enrichFilms(Collection<Film> films) {
        if (films.isEmpty()) {
            return films;
        }

        List<Integer> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());
        log.debug("Получили список id фильмов: {}", filmIds);
        Map<Integer, Set<Genre>> genresMap = genreDbStorage.getGenresForAllFilms(filmIds);
        Map<Integer, Set<Director>> directorsMap = directorDbStorage.getDirectorsForAllFilms(filmIds);
        log.debug("Получили таблицы связей фильмов с жанрами и режиссёрами");

        return films.stream()
                .peek(film -> film.setGenres(genresMap.getOrDefault(film.getId(), new HashSet<>())))
                .peek(film -> film.setDirectors(directorsMap.getOrDefault(film.getId(), new HashSet<>())))
                .toList();
    }

    public Film enrichFilm(Film film) {
        film.setGenres(genreDbStorage.getGenresForOneFilm(film.getId()));
        film.setDirectors(directorDbStorage.getDirectorsForOneFilm(film.getId()));
        return film;
    }
}
