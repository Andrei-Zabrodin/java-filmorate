package ru.yandex.practicum.filmorate.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FilmSortBy;

@Component
public class FilmSortByConverter implements Converter<String, FilmSortBy> {

    @Override
    public FilmSortBy convert(String source) {
        return FilmSortBy.from(source);
    }
}
