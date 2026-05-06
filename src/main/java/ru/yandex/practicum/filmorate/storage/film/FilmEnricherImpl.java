package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Comparator;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class FilmEnricherImpl implements FilmEnricher {
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;

    @Override
    public Collection<Film> enrichFilms(Collection<Film> films) {
        if (films.isEmpty()) {
            return films;
        }

        List<Integer> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());
        Map<Integer, Set<Genre>> genresMap = genreStorage.getGenresForAllFilms(filmIds);
        Map<Integer, Set<Director>> directorsMap = directorStorage.getDirectorsForAllFilms(filmIds);

        return films.stream()
                .peek(film -> film.setGenres(sortedGenres(genresMap.getOrDefault(film.getId(), new HashSet<>()))))
                .peek(film -> film.setDirectors(sortedDirectors(directorsMap.getOrDefault(film.getId(), new HashSet<>()))))
                .toList();
    }

    @Override
    public Film enrichFilm(Film film) {
        film.setGenres(sortedGenres(genreStorage.getGenresForOneFilm(film.getId())));
        film.setDirectors(sortedDirectors(directorStorage.getDirectorsForOneFilm(film.getId())));
        return film;
    }

    private Set<Genre> sortedGenres(Set<Genre> genres) {
        return genres.stream()
                .sorted(Comparator.comparingInt(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<Director> sortedDirectors(Set<Director> directors) {
        return directors.stream()
                .sorted(Comparator.comparingInt(Director::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
