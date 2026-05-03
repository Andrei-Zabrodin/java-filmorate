package ru.yandex.practicum.filmorate.model;

import ru.yandex.practicum.filmorate.exception.ValidateException;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum FilmSearchBy {
    TITLE("title"),
    DIRECTOR("director");
    private final String value;
    FilmSearchBy(String value) {
        this.value = value;
    }

    public static Set<FilmSearchBy> parse(String value) {
        if (value == null || value.isEmpty()) {
            throw new ValidateException("Параметр by не может быть пустым");
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .map(v -> {
                    for (FilmSearchBy searchBy : values()) {
                        if (searchBy.value.equalsIgnoreCase(v)) {
                            return searchBy;
                        }
                    }
                    throw new ValidateException("Параметр by может принимать только: title или director");
                })
                .collect(Collectors.toSet());
    }
}