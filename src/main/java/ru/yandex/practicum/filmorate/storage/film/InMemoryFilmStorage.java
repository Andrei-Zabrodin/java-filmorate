package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmSortBy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@Getter
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private int currentId = 0;

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
    public Film addFilm(Film film) {
        film.setId(++currentId);
        films.put(film.getId(), film);
        log.debug("Добавлен фильм с id {}", film.getId());
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        checkFilmId(newFilm.getId());

        Film oldFilm = films.get(newFilm.getId());

        Optional.ofNullable(newFilm.getName()).ifPresent(oldFilm::setName);
        Optional.ofNullable(newFilm.getDescription()).ifPresent(oldFilm::setDescription);
        Optional.ofNullable(newFilm.getReleaseDate()).ifPresent(oldFilm::setReleaseDate);
        if (newFilm.getDuration() != null) {
            oldFilm.setDuration(newFilm.getDuration());
        }
        if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
            oldFilm.setGenres(newFilm.getGenres());
        }
        if (newFilm.getDirectors() != null && !newFilm.getDirectors().isEmpty()) {
            oldFilm.setDirectors(newFilm.getDirectors());
        }

        log.debug("Фильм с id {} обновлен", newFilm.getId());
        return oldFilm;
    }

    @Override
    public Film deleteFilm(int id) {
        checkFilmId(id);

        log.debug("Удаляем фильм с id {}", id);
        return films.remove(id);
    }

    public void checkFilmId(int id) {
        if (!films.containsKey(id)) {
            log.debug("Не удалось найти фильм с указанным id");
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
    }

    @Override
    public Collection<Film> getFilmsByDirector(int directorId, FilmSortBy sortBy) {
        List<Film> result = films.values().stream()
                .filter(film -> film.getDirectors() != null
                        && film.getDirectors().stream().map(Director::getId).anyMatch(id -> id == directorId))
                .sorted(getComparator(sortBy))
                .collect(Collectors.toList());
        return result;
    }

    private Comparator<Film> getComparator(FilmSortBy sortBy) {
        if (FilmSortBy.LIKES.equals(sortBy)) {
            return Comparator.comparingInt(Film::getLikesAmount).reversed();
        }
        return Comparator.comparing(Film::getReleaseDate);
    }
}
