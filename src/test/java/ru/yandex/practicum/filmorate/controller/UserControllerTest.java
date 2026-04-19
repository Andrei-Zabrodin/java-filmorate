package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Disabled
@WebMvcTest(UserController.class)
public class UserControllerTest {
    private static final String PATH = "/users";

    @Autowired
    private MockMvc mvc;

    @Test
    void postUser_whenCorrect_returnsRequest() throws Exception {
        String request = "{" +
                "\"email\":\"testmail@gmail.com\"," +
                "\"login\":\"Test12345\"," +
                "\"name\":\"George\"," +
                "\"birthday\":\"1985-05-22\"" +
                "}";
        String expectedResponse = "{" +
                "\"id\":2," +
                "\"email\":\"testmail@gmail.com\"," +
                "\"login\":\"Test12345\"," +
                "\"name\":\"George\"," +
                "\"birthday\":\"1985-05-22\"" +
                "}";

        String response = mvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals(expectedResponse, response);
    }

    @Test
    void postUser_whenEmptyName_returnsRequestWithLoginAsName() throws Exception {
        String request = "{" +
                "\"email\":\"testmail@gmail.com\"," +
                "\"login\":\"Test12345\"," +
                "\"name\":\"\"," +
                "\"birthday\":\"1985-05-22\"" +
                "}";

        String expectedResponse = "{" +
                "\"id\":1," +
                "\"email\":\"testmail@gmail.com\"," +
                "\"login\":\"Test12345\"," +
                "\"name\":\"Test12345\"," +
                "\"birthday\":\"1985-05-22\"" +
                "}";

        String response = mvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals(expectedResponse, response);
    }

    @Test
    void postUser_whenEmptyEmail_isBadRequest() throws Exception {
        String request = "{" +
                "\"email\":\"\"," +
                "\"login\":\"Test12345\"," +
                "\"name\":\"George\"," +
                "\"birthday\":\"1985-05-22\"" +
                "}";

        mvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postUser_whenWrongEmail_isBadRequest() throws Exception {
        String request = "{" +
                "\"email\":\"someemail@\"," +
                "\"login\":\"Test12345\"," +
                "\"name\":\"George\"," +
                "\"birthday\":\"1985-05-22\"" +
                "}";

        mvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postUser_whenEmptyLogin_isBadRequest() throws Exception {
        String request = "{" +
                "\"email\":\"testmail@gmail.com\"," +
                "\"login\":\"\"," +
                "\"name\":\"George\"," +
                "\"birthday\":\"1985-05-22\"" +
                "}";

        mvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postUser_whenLoginWithSpaces_isBadRequest() throws Exception {
        String request = "{" +
                "\"email\":\"testmail@gmail.com\"," +
                "\"login\":\"Test 12345\"," +
                "\"name\":\"George\"," +
                "\"birthday\":\"1985-05-22\"" +
                "}";

        mvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postUser_whenWrongBirthdate_isBadRequest() throws Exception {
        String request = "{" +
                "\"email\":\"testmail@gmail.com\"," +
                "\"login\":\"Test12345\"," +
                "\"name\":\"George\"," +
                "\"birthday\":\"2027-05-22\"" +
                "}";

        mvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }
}