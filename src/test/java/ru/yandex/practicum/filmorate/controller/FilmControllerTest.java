package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:clean.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class FilmControllerTest {
    private static final String PATH = "/films";

    @Autowired
    private MockMvc mvc;

    @Test
    void postFilm_whenCorrect_returnsRequest() throws Exception {
        String request = "{" +
                "\"name\":\"Расёмон\"," +
                "\"description\":\"Фильм Акиры Куросавы\"," +
                "\"releaseDate\":\"1950-04-28\"," +
                "\"duration\":88," +
                "\"mpa\":{\"id\":2}" +
                "}";

        String expectedResponse = "{" +
                "\"id\":1," +
                "\"name\":\"Расёмон\"," +
                "\"description\":\"Фильм Акиры Куросавы\"," +
                "\"releaseDate\":\"1950-04-28\"," +
                "\"duration\":88," +
                "\"genres\":[]," +
                "\"mpa\":{" +
                    "\"id\":2," +
                    "\"name\":null}" +
                "}";

        String response = mvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals(expectedResponse, response);
    }

    @Test
    void postFilm_whenEmptyName_isBadRequest() throws Exception {
        String request = "{" +
                "\"name\":\"\"," +
                "\"description\":\"Фильм Акиры Куросавы\"," +
                "\"releaseDate\":\"1950-04-28\"," +
                "\"duration\":88" +
                "}";

        mvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postFilm_whenDescriptionLongerThan200_isBadRequest() throws Exception {
        String longDescription = """
            японский чёрно-белый художественный фильм режиссёра Акиры Куросавы, снятый им вместе с оператором Кадзуо Миягавой
            в жанре дзидайгэки. В фильме снимались такие звёзды японского кино, как Тосиро Мифунэ, Матико Кё,
            Масаюки Мори и Такаси Симура.
            """;

        String request = "{" +
                "\"name\":\"Расёмон\"," +
                "\"description\":\"" + longDescription + "\"," +
                "\"releaseDate\":\"1950-04-28\"," +
                "\"duration\":88" +
                "}";

        mvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postFilm_whenWrongReleaseDate_isBadRequest() throws Exception {
        String request = "{" +
                "\"name\":\"Расёмон\"," +
                "\"description\":\"Фильм Акиры Куросавы\"," +
                "\"releaseDate\":\"1880-02-02\"," +
                "\"duration\":88" +
                "}";

        mvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postFilm_whenNegativeDuration_isBadRequest() throws Exception {
        String request = "{" +
                "\"name\":\"\"," +
                "\"description\":\"Фильм Акиры Куросавы\"," +
                "\"releaseDate\":\"1950-04-28\"," +
                "\"duration\":-80" +
                "}";

        mvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }
}