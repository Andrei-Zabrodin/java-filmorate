package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidateException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;

    public Collection<Director> getDirectors() {
        return directorStorage.getDirectors();
    }

    public Director getDirectorById(int id) {
        return directorStorage.getDirectorById(id)
                .orElseThrow(() -> new NotFoundException("Режиссёр с id " + id + " не найден"));
    }

    public Director addDirector(Director director) {
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        if (director.getId() == 0) {
            throw new ValidateException("Необходимо указать id режиссёра!");
        }
        getDirectorById(director.getId());

        directorStorage.updateDirector(director);

        return getDirectorById(director.getId());
    }

    public Director deleteDirector(int id) {
        Director director = getDirectorById(id);
        directorStorage.deleteDirector(id);
        return director;
    }
}
