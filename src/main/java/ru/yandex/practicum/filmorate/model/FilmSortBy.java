package ru.yandex.practicum.filmorate.model;

public enum FilmSortBy {
    year("year"),
    LIKES("likes");

    private final String value;

    FilmSortBy(String value) {
        this.value = value;
    }

    public static FilmSortBy from(String value) {
        for (FilmSortBy sortBy : values()) {
            if (sortBy.value.equalsIgnoreCase(value)) {
                return sortBy;
            }
        }

        throw new IllegalArgumentException(
                "Параметр sortBy может принимать только: year или likes"
        );
    }
}
