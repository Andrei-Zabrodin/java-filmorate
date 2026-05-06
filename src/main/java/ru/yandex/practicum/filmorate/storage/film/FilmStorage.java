package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmSearchBy;
import ru.yandex.practicum.filmorate.model.FilmSortBy;
import java.util.Collection;
import java.util.Set;

public interface FilmStorage {

    Collection<Film> getFilms();

    Film getFilmById(int id);

    Collection<Film> getRecommendations(int userId);

    Collection<Film> getCommonFilms(int userId, int friendId);

    Film addFilm(Film film);

    Film updateFilm(Film newFilm);

    Film deleteFilm(int id);

    Collection<Film> getFilmsByDirector(int directorId, FilmSortBy sortBy);

    Collection<Film> searchFilms(String query, Set<FilmSearchBy> searchBy);

    void checkFilmExistence(int id);
}
