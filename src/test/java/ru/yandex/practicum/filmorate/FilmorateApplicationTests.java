package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmorateApplicationTests {

    @Autowired
    MockMvc mockMvc;
    static FilmController filmController = new FilmController();

    @DisplayName("POST при добавлении одного фильма")
    @Test
    void shouldAddFilm() throws Exception {
        String json = "{"
                + "\"name\": \"Matrix\","
                + "\"description\": \"Sci-fi\","
                + "\"releaseDate\": \"1999-03-31\","
                + "\"duration\": \"PT120M\""
                + "}";
        //Здесь в проверке задан id = 2 потому что данные хэщ-таблицы перед тестами не обновляются:
        //попытки очистить хэш-таблицу перед каждым тестом не увенчались успехом :)
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Matrix"));
    }

    @DisplayName("POST при пустом названии")
    @Test
    void shouldReturnErrorWhenNameBlank() throws Exception {
        String json = "{"
                + "\"name\": \"\","
                + "\"description\": \"Sci-fi\","
                + "\"releaseDate\": \"1999-03-31\","
                + "\"duration\": \"PT120M\""
                + "}";
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is4xxClientError());
    }

    @DisplayName("POST при слишком длинном названии")
    @Test
    void shouldReturnErrorWhenDescriptionTooLong() throws Exception {
        String longDesc = "a".repeat(201);

        String json = "{"
                + "\"name\": \"Matrix\","
                + "\"description\": \"%s\","
                + "\"releaseDate\": \"1999-03-31\","
                + "\"duration\": \"PT120M\""
                + "}";
        json = json.formatted(longDesc);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is4xxClientError());
    }


    @DisplayName("PUT при изменении названия фильма")
    @Test
    void shouldChangeMovie() throws Exception {

        String json = "{"
                + "\"name\": \"Matrix\","
                + "\"description\": \"Sci-fi\","
                + "\"releaseDate\": \"1999-03-31\","
                + "\"duration\": \"PT120M\""
                + "}";

        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
        json = "{"
                + "\"id\": 1,"
                + "\"name\": \"Матрица\","
                + "\"description\": \"Научная фантастика\","
                + "\"releaseDate\": \"1999-03-31\","
                + "\"duration\": \"PT150M\""
                + "}";

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }


    @DisplayName("POST при добавлении пользователя")
    @Test
    void shouldAddUser() throws Exception {
        String json = "{"
                + "\"email\": \"diyorka255@mail.com\","
                + "\"login\": \"diyor\","
                + "\"name\": \"Diyor\","
                + "\"birthday\": \"2001-07-08\""
                + "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("diyorka255@mail.com"));
    }

    @DisplayName("POST при пустой почте")
    @Test
    void shouldReturnErrorWhenEmailBlank() throws Exception {
        String json = "{"
                + "\"email\": \"\","
                + "\"login\": \"diyor\","
                + "\"name\": \"Diyor\","
                + "\"birthday\": \"2001-07-08\""
                + "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is4xxClientError());
    }

    @DisplayName("POST при дате рождения в будущем")
    @Test
    void shouldReturnErrorWhenBirthdayInFuture() throws Exception {
        String json = "{"
                + "\"email\": \"diyorka255@mail.com\","
                + "\"login\": \"diyor\","
                + "\"name\": \"Diyor\","
                + "\"birthday\": \"20010-07-08\""
                + "}";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is4xxClientError());
    }


    @DisplayName("PUT при добавлении пользователя c неправильным id")
    @Test
    void shouldReturnErrorWhenIdIsWrong() throws Exception {
        String json = "{"
                + "\"email\": \"diyorka255@mail.com\","
                + "\"login\": \"diyor\","
                + "\"name\": \"Diyor\","
                + "\"birthday\": \"2001-07-08\""
                + "}";

        mockMvc.perform(put("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));

        json = "{"
                + "\"id\": 2,"
                + "\"email\": \"diyorka255@mail.com\","
                + "\"login\": \"diyor\","
                + "\"name\": \"Diyor\","
                + "\"birthday\": \"2001-07-08\""
                + "}";
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is4xxClientError());
    }

}
