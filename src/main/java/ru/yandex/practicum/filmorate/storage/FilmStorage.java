package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    Collection<Film> getFilms();

    Film getFilmById(int id);

    Film addFilm(Film film);

    Film updateFilm(Film newFilm);

    Film deleteFilm(int id);
}
