package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DatabaseException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.DbStorage;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.rating.RatingStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

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
            " ORDER BY count DESC, f.release_date DESC";
    private static final String GET_RECOMMENDATION_BY_USER_ID_QUERY = "WITH most_similar_user AS " +
            "(SELECT l2.user_id FROM likes l1 JOIN likes l2 USING(film_id) WHERE l1.user_id = ? AND l2.user_id != ? " +
            "GROUP BY l2.user_id ORDER BY COUNT(*) DESC LIMIT 1), " +
            "recommended_film_ids AS (SELECT film_id FROM likes WHERE user_id = (SELECT user_id FROM most_similar_user) " +
            "EXCEPT SELECT film_id FROM likes WHERE user_id = ?) " +
            "SELECT f.*, r.name as rating_name FROM films f " +
            "JOIN ratings r USING (rating_id) " +
            "WHERE film_id IN (SELECT film_id FROM recommended_film_ids)";
    private static final String GET_COMMON_FILMS_QUERY = "SELECT f.*, r.name AS rating_name FROM films f " +
            "JOIN (SELECT film_id FROM likes WHERE user_id IN (?, ?) " +
            "GROUP BY film_id HAVING COUNT(user_id) > 1) common_films USING(film_id) " +
            "JOIN (SELECT film_id, count(user_id) AS like_count FROM likes GROUP BY film_id) l USING(film_id)" +
            "JOIN ratings r USING (rating_id)" +
            "ORDER BY l.like_count DESC";
    private static final String ADD_FILM_QUERY = "INSERT INTO films(name, description, release_date, duration," +
            " rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM_QUERY_START = "UPDATE films SET ";
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE film_id = ?";
    private static final String SEARCH_BY_TITLE_QUERY =
            "SELECT f.*, r.name AS rating_name FROM films f " +
                    "JOIN ratings r USING (rating_id) " +
                    "WHERE UPPER(f.name) LIKE UPPER(?) " +
                    "ORDER BY (SELECT COUNT(*) FROM likes WHERE film_id = f.film_id) DESC, f.film_id";
    private static final String SEARCH_BY_DIRECTOR_QUERY =
            "SELECT f.*, r.name AS rating_name FROM films f " +
                    "JOIN ratings r USING (rating_id) " +
                    "JOIN films_directors fd ON f.film_id = fd.film_id " +
                    "JOIN directors d ON fd.director_id = d.director_id " +
                    "WHERE UPPER(d.name) LIKE UPPER(?) " +
                    "ORDER BY (SELECT COUNT(*) FROM likes WHERE film_id = f.film_id) DESC, f.film_id";
    private static final String SEARCH_BY_TITLE_AND_DIRECTOR_QUERY =
            "SELECT f.*, r.name AS rating_name, " +
                    "(SELECT COUNT(*) FROM likes WHERE film_id = f.film_id) AS like_count " +
                    "FROM films f " +
                    "JOIN ratings r USING (rating_id) " +
                    "LEFT JOIN films_directors fd ON f.film_id = fd.film_id " +
                    "LEFT JOIN directors d ON fd.director_id = d.director_id " +
                    "WHERE UPPER(f.name) LIKE UPPER(?) OR UPPER(d.name) LIKE UPPER(?) " +
                    "GROUP BY f.film_id, r.name " +
                    "ORDER BY like_count DESC, f.film_id";
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;
    private final RatingStorage ratingStorage;
    private final FilmEnricher filmEnricher;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, @Qualifier("userDbStorage") UserStorage userStorage,
                         GenreStorage genreStorage, DirectorStorage directorStorage, RatingStorage ratingStorage,
                         FilmEnricher filmEnricher) {
        super(jdbc, mapper);
        this.userStorage = userStorage;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
        this.ratingStorage = ratingStorage;
        this.filmEnricher = filmEnricher;
    }

    @Override
    public Collection<Film> getFilms() {
        log.debug("Возвращаем список фильмов");
        return filmEnricher.enrichFilms(findMany(GET_ALL_FILMS_QUERY));
    }

    @Override
    public Film getFilmById(int id) {
        Optional<Film> filmOpt = findOne(GET_FILM_BY_ID_QUERY, id);
        if (filmOpt.isPresent()) {
            log.debug("Нашли фильм с id {}", id);
            return filmEnricher.enrichFilm(filmOpt.get());
        } else {
            log.debug("Не удалось найти фильм с указанным id");
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
    }

    @Override
    public Collection<Film> getRecommendations(int userId) {
        //Проверяем наличие пользователя
        userStorage.checkUserExistence(userId);

        return filmEnricher.enrichFilms(findMany(GET_RECOMMENDATION_BY_USER_ID_QUERY, userId, userId, userId));
    }

    @Override
    public Collection<Film> getCommonFilms(int userId, int friendId) {
        userStorage.checkUserExistence(userId);
        userStorage.checkUserExistence(friendId);

        log.debug("Возвращаем список общих фильмов пользователей с id {} и {}", userId, friendId);
        return filmEnricher.enrichFilms(findMany(GET_COMMON_FILMS_QUERY, userId, friendId));
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
            genreStorage.addFilmGenres(id.get(), genreIds);
            directorStorage.addFilmDirectors(id.get(), directorIds);

            log.debug("Добавлен фильм с id {}", film.getId());
            return getFilmById(id.get());
        } else {
            throw new DatabaseException("Не удалось добавить данные");
        }
    }

    @Override
    public Film updateFilm(Film newFilm) {
        checkFilmExistence(newFilm.getId());

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

        genreStorage.deleteFilmGenres(newFilm.getId());
        if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
            Set<Integer> genreIds = newFilm.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            genreStorage.addFilmGenres(newFilm.getId(), genreIds);
        }

        directorStorage.deleteFilmDirectors(newFilm.getId());
        if (newFilm.getDirectors() != null && !newFilm.getDirectors().isEmpty()) {
            Set<Integer> directorIds = newFilm.getDirectors().stream()
                    .map(Director::getId)
                    .collect(Collectors.toSet());
            directorStorage.addFilmDirectors(newFilm.getId(), directorIds);
        }

        Film film = getFilmById(newFilm.getId());
        log.debug("Фильм с id {} обновлен", newFilm.getId());
        return film;
    }

    @Override
    public Film deleteFilm(int id) {
        Film film = getFilmById(id);
        log.debug("Удаляем фильм с id {}", id);
        delete(DELETE_FILM_QUERY, id);

        return film;
    }

    @Override
    public void checkFilmExistence(int filmId) {
        getFilmById(filmId);
    }

    @Override
    public Collection<Film> getFilmsByDirector(int directorId, FilmSortBy sortBy) {
        directorStorage.getDirectorById(directorId);

        Collection<Film> films = FilmSortBy.LIKES.equals(sortBy)
                ? findMany(GET_FILMS_BY_DIRECTOR_SORT_BY_LIKES_QUERY, directorId)
                : findMany(GET_FILMS_BY_DIRECTOR_SORT_BY_YEAR_QUERY, directorId);

        if (films.isEmpty()) {
            throw new NotFoundException("Фильмы режиссёра с id " + directorId + " не найдены");
        }

        return filmEnricher.enrichFilms(films);
    }

    @Override
    public Collection<Film> searchFilms(String query, Set<FilmSearchBy> searchBy) {
        String searchPattern = "%" + query + "%";
        Collection<Film> films;
        if (searchBy.contains(FilmSearchBy.TITLE) && searchBy.contains(FilmSearchBy.DIRECTOR)) {
            films = findMany(SEARCH_BY_TITLE_AND_DIRECTOR_QUERY, searchPattern, searchPattern);
        } else if (searchBy.contains(FilmSearchBy.TITLE)) {
            films = findMany(SEARCH_BY_TITLE_QUERY, searchPattern);
        } else if (searchBy.contains(FilmSearchBy.DIRECTOR)) {
            films = findMany(SEARCH_BY_DIRECTOR_QUERY, searchPattern);
        } else {
            throw new ValidateException("Параметр by должен содержать хотя бы одно значение: title или director");
        }
        return filmEnricher.enrichFilms(films);
    }
}
