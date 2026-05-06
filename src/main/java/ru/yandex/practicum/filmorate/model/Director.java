package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Director {
    private int id;

    @NotBlank(message = "У режиссёра должно быть заполнено имя")
    private String name;
}
