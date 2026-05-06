package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@RestController
@RequestMapping("/directors")
@Slf4j
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;

    @GetMapping
    public Collection<Director> getDirectors() {
        return directorService.getDirectors();
    }

    @GetMapping("/{id}")
    public Director getDirector(@PathVariable int id) {
        return directorService.getDirectorById(id);
    }

    @PostMapping
    public Director postDrector(@Valid @RequestBody Director director) {
        return directorService.addDirector(director);
    }

    @PutMapping
    public Director updateDirector(@Valid @RequestBody Director director) {
        return directorService.updateDirector(director);
    }

    @DeleteMapping("/{id}")
    public void deleteDirector(@PathVariable int id) {
        log.debug("Удаление режиссёра с id {}", id);
        directorService.deleteDirector(id);
    }
}
