package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;

public interface DirectorStorage {
    Collection<Director> getDirectors();

    Optional<Director> getDirectorById(int id);

    Director addDirector(Director director);

    void updateDirector(Director director);

    void deleteDirector(int id);
}
