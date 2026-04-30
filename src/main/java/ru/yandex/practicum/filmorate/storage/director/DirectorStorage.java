package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.*;

public interface DirectorStorage {
    Collection<Director> getDirectors();

    Optional<Director> getDirectorById(int id);

    Director addDirector(Director director);

    void updateDirector(Director director);

    void deleteDirector(int id);

    Set<Director> getDirectorsForOneFilm(int filmId);

    Map<Integer, Set<Director>> getDirectorsForAllFilms(List<Integer> filmIds);

    void addFilmDirectors(int filmId, Set<Integer> directorIds);
}
