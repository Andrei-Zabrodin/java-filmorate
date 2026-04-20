package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.LikeStorage;

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

    public Collection<Film> getPopularFilms(int count) {
        log.debug("Начинам формировать список {} популярных фильмов", count);
        return likeStorage.getPopularFilms(count);
    }
}
