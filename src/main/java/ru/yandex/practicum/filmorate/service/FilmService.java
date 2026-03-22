package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film newFilm) {
        log.debug("Начинаем исправление записи с id фильма {}", newFilm.getId());
        if (newFilm.getId() == 0) {
            log.debug("В запросе не указан id фильма");
            throw new ValidateException("Необходимо указать id фильма!");
        }

        return filmStorage.updateFilm(newFilm);
    }

    public Film deleteFilm(int id) {
        return filmStorage.deleteFilm(id);
    }

    public void likeFilm(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        log.debug("Ставим лайк фильму с id {} от пользователя с id {}", filmId, userId);
        film.getThoseWhoLiked().add(user);
    }

    public void deleteLike(int filmId, int userId) {
        Film film = filmStorage.getFilmById(filmId);
        User user = userStorage.getUserById(userId);

        log.debug("Удаляем фильму с id {} лайк от пользователя с id {}", filmId, userId);
        film.getThoseWhoLiked().remove(user);
    }

    public Collection<Film> getPopularFilms(int count) {
        log.debug("Начинам формировать список {} популярных фильмов", count);
        return filmStorage.getFilms().stream()
                .sorted(Comparator.comparing(Film::getLikesAmount).reversed())
                .limit(count)
                .toList();
    }


}
