package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        UserDbStorage.class,
        FilmDbStorage.class,
        GenreDbStorage.class,
        MpaDbStorage.class
})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmorateApplicationTests {

    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final GenreDbStorage genreStorage;
    private final MpaDbStorage mpaStorage;

    //Тесты для проверки UserDbStorage
    @Test
    void shouldAddAndFindUserById() {
        User user = new User();
        user.setEmail("diyorka255@mail.ru");
        user.setLogin("Diyorka");
        user.setName("Diyor");
        user.setBirthday(LocalDate.of(2001, 7, 8));

        User createdUser = userStorage.addUser(user);
        User foundUser = userStorage.getUserById(createdUser.getId());

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo("diyorka255@mail.ru");
    }

    @Test
    void shouldUpdateUser() {
        User user = new User();
        user.setEmail("diyorka255@mail.ru");
        user.setLogin("Diyorka");
        user.setName("Diyor");
        user.setBirthday(LocalDate.of(2001, 7, 8));

        User createdUser = userStorage.addUser(user);

        createdUser.setEmail("newDiyorka255@mail.ru");
        createdUser.setName("New Diyor");
        userStorage.changeUser(createdUser);

        User updatedUser = userStorage.getUserById(createdUser.getId());

        assertThat(updatedUser.getEmail()).isEqualTo("newDiyorka255@mail.ru");
        assertThat(updatedUser.getName()).isEqualTo("New Diyor");
    }

    @Test
    void shouldAddFriend() {
        User user1 = new User();
        user1.setEmail("AlexandrFedyorov@mail.ru");
        user1.setLogin("Review");
        user1.setName("Alexander");
        user1.setBirthday(LocalDate.of(2000, 1, 1));

        User user2 = new User();
        user2.setEmail("diyorka255@mail.ru");
        user2.setLogin("Diyorka");
        user2.setName("Diyor");
        user2.setBirthday(LocalDate.of(2001, 7, 8));

        User createdUser1 = userStorage.addUser(user1);
        User createdUser2 = userStorage.addUser(user2);

        userStorage.addFriend(createdUser1.getId(), createdUser2.getId());

        User userFromDb = userStorage.getUserById(createdUser1.getId());

        assertThat(userFromDb.getFriends()).contains(createdUser2.getId());
    }

    @Test
    void shouldDeleteFriend() {
        User user1 = new User();
        user1.setEmail("AlexandrFedyorov@mail.ru");
        user1.setLogin("Review");
        user1.setName("Alexander");
        user1.setBirthday(LocalDate.of(2000, 1, 1));

        User user2 = new User();
        user2.setEmail("diyorka255@mail.ru");
        user2.setLogin("Diyorka");
        user2.setName("Diyor");
        user2.setBirthday(LocalDate.of(2001, 7, 8));


        User createdUser1 = userStorage.addUser(user1);
        User createdUser2 = userStorage.addUser(user2);

        userStorage.addFriend(createdUser1.getId(), createdUser2.getId());
        userStorage.deleteFriend(createdUser1.getId(), createdUser2.getId());

        User userFromDb = userStorage.getUserById(createdUser1.getId());

        assertThat(userFromDb.getFriends()).doesNotContain(createdUser2.getId());
    }

    @Test
    void shouldReturnAllFriends() {
        User user1 = new User();
        user1.setEmail("AlexandrFedyorov@mail.ru");
        user1.setLogin("Review");
        user1.setName("Alexander");
        user1.setBirthday(LocalDate.of(2000, 1, 1));

        User user2 = new User();
        user2.setEmail("diyorka255@mail.ru");
        user2.setLogin("Diyorka");
        user2.setName("Diyor");
        user2.setBirthday(LocalDate.of(2001, 7, 8));


        User createdUser1 = userStorage.addUser(user1);
        User createdUser2 = userStorage.addUser(user2);

        userStorage.addFriend(createdUser1.getId(), createdUser2.getId());

        Collection<User> friends = userStorage.getAllFriends(createdUser1.getId());

        assertThat(friends).hasSize(1);
        assertThat(friends.iterator().next().getId()).isEqualTo(createdUser2.getId());
    }

    @Test
    void shouldReturnCommonFriends() {
        User user1 = new User();
        user1.setEmail("AlexandrFedyorov@mail.ru");
        user1.setLogin("Review");
        user1.setName("Alexander");
        user1.setBirthday(LocalDate.of(2000, 1, 1));

        User user2 = new User();
        user2.setEmail("diyorka255@mail.ru");
        user2.setLogin("Diyorka");
        user2.setName("Diyor");
        user2.setBirthday(LocalDate.of(2001, 7, 8));


        User common = new User();
        common.setEmail("common@test.com");
        common.setLogin("common");
        common.setName("Common");
        common.setBirthday(LocalDate.of(2002, 1, 1));

        User createdUser1 = userStorage.addUser(user1);
        User createdUser2 = userStorage.addUser(user2);
        User createdCommon = userStorage.addUser(common);

        userStorage.addFriend(createdUser1.getId(), createdCommon.getId());
        userStorage.addFriend(createdUser2.getId(), createdCommon.getId());

        Collection<User> commonFriends = userStorage.getCommonFriends(createdUser1.getId(), createdUser2.getId());

        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.iterator().next().getId()).isEqualTo(createdCommon.getId());
    }

    //Тесты для проверки FilmDbStorage

    @Test
    void shouldAddAndFindFilmById() {
        Film film = createFilm("Matrix");

        Film createdFilm = filmStorage.addFilm(film);
        Film foundFilm = filmStorage.getFilmById(createdFilm.getId());

        assertThat(foundFilm).isNotNull();
        assertThat(foundFilm.getId()).isEqualTo(createdFilm.getId());
        assertThat(foundFilm.getName()).isEqualTo("Matrix");
        assertThat(foundFilm.getMpa()).isNotNull();
        assertThat(foundFilm.getMpa().getId()).isEqualTo(1L);
    }

    @Test
    void shouldUpdateFilm() {
        Film film = createFilm("Old Name");
        Film createdFilm = filmStorage.addFilm(film);

        createdFilm.setName("New Name");
        createdFilm.setDescription("New Description");
        filmStorage.changeFilm(createdFilm);

        Film updatedFilm = filmStorage.getFilmById(createdFilm.getId());

        assertThat(updatedFilm.getName()).isEqualTo("New Name");
        assertThat(updatedFilm.getDescription()).isEqualTo("New Description");
    }

    @Test
    void shouldAddLike() {
        User user = createUser("user@test.com", "user");
        User createdUser = userStorage.addUser(user);

        Film film = createFilm("Film");
        Film createdFilm = filmStorage.addFilm(film);

        filmStorage.addLike(createdFilm.getId(), createdUser.getId());

        Film filmFromDb = filmStorage.getFilmById(createdFilm.getId());

        assertThat(filmFromDb.getUsersId()).contains(createdUser.getId());
    }

    @Test
    void shouldDeleteLike() {
        User user = createUser("user@test.com", "user");
        User createdUser = userStorage.addUser(user);

        Film film = createFilm("Film");
        Film createdFilm = filmStorage.addFilm(film);

        filmStorage.addLike(createdFilm.getId(), createdUser.getId());
        filmStorage.deleteLike(createdFilm.getId(), createdUser.getId());

        Film filmFromDb = filmStorage.getFilmById(createdFilm.getId());

        assertThat(filmFromDb.getUsersId()).doesNotContain(createdUser.getId());
    }

    @Test
    void shouldReturnPopularFilms() {
        User user1 = userStorage.addUser(createUser("u1@test.com", "u1"));
        User user2 = userStorage.addUser(createUser("u2@test.com", "u2"));

        Film film1 = filmStorage.addFilm(createFilm("Film 1"));
        Film film2 = filmStorage.addFilm(createFilm("Film 2"));

        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film1.getId(), user2.getId());
        filmStorage.addLike(film2.getId(), user1.getId());

        Collection<Film> popularFilms = filmStorage.getPopularFilms(10);

        Film firstFilm = popularFilms.iterator().next();
        assertThat(firstFilm.getId()).isEqualTo(film1.getId());
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        film.setMpa(mpa);

        return film;
    }

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }

    //Тесты для проверки GenreDbStorage
    @Test
    void shouldReturnAllGenres() {
        Collection<Genre> genres = genreStorage.getAllGenres();
        assertThat(genres).isNotEmpty();
        assertThat(genres).hasSize(6);
    }

    @Test
    void shouldReturnGenreById() {
        Genre genre = genreStorage.getGenreById(1L);
        assertThat(genre.getId()).isEqualTo(1L);
        assertThat(genre.getName()).isEqualTo("Комедия");
    }

    //Тесты для проверки MpaDbStorage
    @Test
    void shouldReturnAllMpa() {
        Collection<Mpa> ratings = mpaStorage.getAllMpa();
        assertThat(ratings).isNotEmpty();
        assertThat(ratings).hasSize(5);
    }

    @Test
    void shouldReturnMpaById() {
        Mpa mpa = mpaStorage.getMpaById(1L);
        assertThat(mpa.getId()).isEqualTo(1L);
        assertThat(mpa.getName()).isEqualTo("G");
    }

    @Test
    void shouldDeleteFilmById() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1L);
        film.setMpa(mpa);

        filmStorage.addFilm(film);

        Long id = film.getId();
        filmStorage.deleteFilmById(id);

        assertThatThrownBy(() -> filmStorage.getFilmById(id)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeleteUserById() {
        User user = new User();
        user.setEmail("yaroslavanferov@yandx.ru");
        user.setLogin("yaroslavanferov");
        user.setName("yaroslv");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        userStorage.addUser(user);
        Long id = user.getId();

        userStorage.deleteUser(id);

        assertThatThrownBy(() -> userStorage.getUserById(id)).isInstanceOf(NotFoundException.class);
    }

}
