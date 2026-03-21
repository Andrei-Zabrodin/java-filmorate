package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.validator.ValidReleaseDate;

import java.time.LocalDate;

@Data
@Slf4j
public class Film {

    private static final int MAX_DESCRIPTION = 200;

    private int id;

    @NotBlank(message = "У фильма должно быть заполнено название")
    private String name;

    @Size(max = MAX_DESCRIPTION, message = "Описание фильма не должно превышать 200 символов!")
    private String description;

    @ValidReleaseDate
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть больше 0!")
    private Integer duration;
}
