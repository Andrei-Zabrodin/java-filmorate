package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmSortBy;

import java.util.Collection;

public interface FilmStorage {

    Collection<Film> getFilms();

    Film getFilmById(int id);

    Collection<Film> getRecommendations(int userId);

    Film addFilm(Film film);

    Film updateFilm(Film newFilm);

    Film deleteFilm(int id);

    Collection<Film> getFilmsByDirector(int directorId, FilmSortBy sortBy);
}
