package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.Collection;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final LikeStorage likeStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, LikeStorage likeStorage) {
        this.filmStorage = filmStorage;
        this.likeStorage = likeStorage;;
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(int filmId) {
        return filmStorage.getFilmById(filmId);
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
        likeStorage.likeFilm(filmId, userId);
    }

    public void deleteLike(int filmId, int userId) {
        likeStorage.deleteLike(filmId, userId);
    }

    public Collection<Film> getPopularFilms(int count) {
        log.debug("Начинам формировать список {} популярных фильмов", count);
        return likeStorage.getPopularFilms(count);
    }
}