package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(value = "/films")
@Slf4j
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private int currentId = 0;

    @GetMapping
    public Collection<Film> getFilms() {
        log.debug("Возвращён список фильмов");
        return films.values();
    }

    @PostMapping
    public Film postFilm(@Valid @RequestBody Film film) {
        film.setId(++currentId);
        films.put(film.getId(), film);
        log.debug("Добавлен фильм с id {}", film.getId());
        return film;
    }

    @PutMapping()
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        log.debug("Начинаем исправление записи с id фильма {}", newFilm.getId());
        if (newFilm.getId() == 0) {
            log.debug("В запросе не указан id фильма");
            throw new ValidateException("Необходимо указать id фильма!");
        }

        if (films.containsKey(newFilm.getId())) {
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

        log.debug("Не удалось найти фильм с указанным id");
        throw new ValidateException("Фильм с id = " + newFilm.getId() + " не найден");
    }
}
