package ru.yandex.practicum.filmorate.storage.genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GenreStorage {

    Collection<Genre> getGenres();

    Genre getGenreById(int id);

    void addFilmGenres(int filmId, Set<Integer> genreIds);

    Set<Genre> getGenresForOneFilm(int filmId);

    Map<Integer, Set<Genre>> getGenresForAllFilms(List<Integer> filmIds);
}