package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Data
@Slf4j
public class User {
    private int id;

    @NotBlank(message = "Электронная почта не должна быть пустой!")
    @Email(message = "Некорректный формат электронной почты!")
    private String email;

    @NotBlank(message = "Логин не должен быть пустой!")
    @Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы!")
    private String login;

    private String name;

    @PastOrPresent(message = "Дата рождения должна быть не позже текущей даты!")
    private LocalDate birthday;
}
