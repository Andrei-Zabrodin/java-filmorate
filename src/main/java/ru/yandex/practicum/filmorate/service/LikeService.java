package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;

import java.util.Collection;

@Service
@Slf4j
@RequiredArgsConstructor
public class LikeService {
    private final LikeStorage likeStorage;

    public void likeFilm(int filmId, int userId) {
        likeStorage.likeFilm(filmId, userId);
    }

    public void deleteLike(int filmId, int userId) {
        likeStorage.deleteLike(filmId, userId);
    }

    public Collection<Film> getPopularFilms(int count, Integer genreId, Integer year) {
        if (genreId != null && genreId <= 0) {
            throw new ValidateException("genreId должен быть положительным");
        }
        if (year != null && year <= 0) {
            throw new ValidateException("year должен быть положительным");
        }

        log.debug("Начинаем формировать список {} популярных фильмов (genreId={}, year={})",
                count, genreId, year);
        return likeStorage.getPopularFilms(count, genreId, year);
    }
}
