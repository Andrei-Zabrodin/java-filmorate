package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmSearchBy;
import ru.yandex.practicum.filmorate.model.FilmSortBy;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Getter
public class InMemoryFilmStorage implements FilmStorage {
    private final UserStorage userStorage;
    private final Map<Integer, Film> films = new HashMap<>();
    private int currentId = 0;

    public InMemoryFilmStorage(@Qualifier("inMemoryUserStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public Collection<Film> getFilms() {
        log.debug("Возвращён список фильмов");
        return films.values();
    }

    @Override
    public Film getFilmById(int id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }

        log.debug("Нашли фильм с id {}", id);
        return films.get(id);
    }

    @Override
    public Collection<Film> getRecommendations(int userId) {
        //Проверяем наличие пользователя
        userStorage.checkUserExistence(userId);

        Map<Integer, Set<Integer>> likesMap = getLikesMap();
        Set<Integer> recommendedFilmsIds = getRecommendedFilmIds(userId, likesMap);

        if (recommendedFilmsIds.isEmpty()) {
            throw new NotFoundException("К сожалению, для пользователя с id " + userId + " нет рекомендаций");
        }

        return films.values().stream()
                .filter(film -> recommendedFilmsIds.contains(film.getId()))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<Film> getCommonFilms(int userId, int friendId) {
        userStorage.checkUserExistence(userId);
        userStorage.checkUserExistence(friendId);

        log.debug("Возвращаем список общих фильмов пользователей с id {} и {}", userId, friendId);
        List<Film> result = films.values().stream()
                .filter(film -> film.getThoseWhoLiked().stream()
                        .anyMatch(user -> user.getId() == userId || user.getId() == friendId))
                .sorted(getComparator(FilmSortBy.LIKES))
                .collect(Collectors.toList());
        return result;
    }

    @Override
    public Film addFilm(Film film) {
        film.setId(++currentId);
        if (film.getGenres() != null) {
            film.setGenres(film.getGenres().stream()
                    .sorted(Comparator.comparingInt(Genre::getId))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }
        if (film.getDirectors() != null) {
            film.setDirectors(film.getDirectors().stream()
                    .sorted(Comparator.comparingInt(Director::getId))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        }
        films.put(film.getId(), film);
        log.debug("Добавлен фильм с id {}", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        checkFilmExistence(newFilm.getId());

        Film oldFilm = films.get(newFilm.getId());

        Optional.ofNullable(newFilm.getName()).ifPresent(oldFilm::setName);
        Optional.ofNullable(newFilm.getDescription()).ifPresent(oldFilm::setDescription);
        Optional.ofNullable(newFilm.getReleaseDate()).ifPresent(oldFilm::setReleaseDate);
        if (newFilm.getDuration() != null) {
            oldFilm.setDuration(newFilm.getDuration());
        }
        if (newFilm.getGenres() != null) {
            if (!newFilm.getGenres().isEmpty()) {
                oldFilm.setGenres(newFilm.getGenres());
            } else {
                oldFilm.setGenres(new HashSet<>());
            }
        } else {
            oldFilm.setGenres(new HashSet<>());
        }
        if (newFilm.getDirectors() != null) {
            if (!newFilm.getDirectors().isEmpty()) {
                oldFilm.setDirectors(newFilm.getDirectors());
            } else {
                oldFilm.setDirectors(new HashSet<>());
            }
        } else {
            oldFilm.setDirectors(new HashSet<>());
        }

        log.debug("Фильм с id {} обновлен", newFilm.getId());
        return oldFilm;
    }

    @Override
    public Film deleteFilm(int id) {
        checkFilmExistence(id);

        log.debug("Удаляем фильм с id {}", id);
        return films.remove(id);
    }

    @Override
    public void checkFilmExistence(int id) {
        if (!films.containsKey(id)) {
            log.debug("Не удалось найти фильм с указанным id");
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
    }

    @Override
    public Collection<Film> getFilmsByDirector(int directorId, FilmSortBy sortBy) {
        List<Film> result = films.values().stream()
                .filter(film -> film.getDirectors() != null
                        && film.getDirectors().stream().anyMatch(director -> director.getId() == directorId))
                .sorted(getComparator(sortBy))
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            throw new NotFoundException("Фильмы режиссёра с id " + directorId + " не найдены");
        }

        return result;
    }

    private Comparator<Film> getComparator(FilmSortBy sortBy) {
        if (FilmSortBy.LIKES.equals(sortBy)) {
            return Comparator.comparingInt(Film::getLikesAmount)
                    .reversed()
                    .thenComparing(Film::getReleaseDate, Comparator.reverseOrder());
        }
        return Comparator.comparing(Film::getReleaseDate);
    }

    //Получаем таблицу, где ключи — id пользователей, значения — множества id фильмов, которые этот пользователь лайкнул
    private Map<Integer, Set<Integer>> getLikesMap() {
        Map<Integer, Set<Integer>> likesMap = new HashMap<>();

        for (Film film : films.values()) {
            for (User user : film.getThoseWhoLiked())
                likesMap.computeIfAbsent(user.getId(), k -> new HashSet<>()).add(film.getId());
        }

        return likesMap;
    }

    //Ищем id пользователя, у которого больше всего совпадений по фильмам с целевым пользователем
    private Set<Integer> getRecommendedFilmIds(int userId, Map<Integer, Set<Integer>> likesMap) {
        Set<Integer> targetUserFilmIds = likesMap.get(userId);

        if (targetUserFilmIds == null || targetUserFilmIds.isEmpty()) {
            throw new NotFoundException("У пользователя с id " + userId + " ещё нет лайков");
        }

        int maxIntersection = 0;
        int mostSimilarUserId = userId;

        for (Map.Entry<Integer, Set<Integer>> likesEntry : likesMap.entrySet()) {
            if (likesEntry.getKey() != userId) {
                Set<Integer> otherUserFilmIds = new HashSet<>(likesEntry.getValue());
                otherUserFilmIds.retainAll(targetUserFilmIds);

                if (otherUserFilmIds.size() > maxIntersection) {
                    maxIntersection = otherUserFilmIds.size();
                    mostSimilarUserId = likesEntry.getKey();
                }
            }
        }

        if (mostSimilarUserId == userId) {
            throw new NotFoundException("У пользователя с id " + userId +
                    " не найдено пересечений по лайкам с другими пользователями");
        }

        Set<Integer> mostSimilarUserFilmIds = new HashSet<>(likesMap.get(mostSimilarUserId));
        mostSimilarUserFilmIds.removeAll(targetUserFilmIds);

        return mostSimilarUserFilmIds;
    }

    @Override
    public Collection<Film> searchFilms(String query, Set<FilmSearchBy> searchBy) {
        // Заглушка
        return Collections.emptyList();
    }
}
