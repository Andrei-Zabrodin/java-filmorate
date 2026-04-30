package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmEnricher {
    Collection<Film> enrichFilms(Collection<Film> films);

    Film enrichFilm(Film film);
}
