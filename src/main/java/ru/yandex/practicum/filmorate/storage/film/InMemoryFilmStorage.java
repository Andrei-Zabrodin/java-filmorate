package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        if (newFilm.getDuration() != 0) {
            oldFilm.setDuration(newFilm.getDuration());
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
}