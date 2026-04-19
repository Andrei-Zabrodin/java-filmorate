package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;

import java.time.LocalDate;
import java.util.*;

        import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = "classpath:test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final FriendDbStorage friendStorage;
    private final LikeDbStorage likeStorage;
    private final GenreDbStorage genreStorage;
    private final RatingDbStorage ratingStorage;
    private final JdbcTemplate jdbc;

    @Test
    public void testFindAllUsers() {
        Collection<User> foundUsers = userStorage.getUsers();

        assertThat(foundUsers).extracting(User::getLogin)
                .containsExactlyInAnyOrder("user1", "user2", "user3");
        assertThat(foundUsers).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com", "user3@example.com");
        assertThat(foundUsers).extracting(User::getName)
                .containsExactlyInAnyOrder("User 1", "User 2", "User 3");
        assertThat(foundUsers).extracting(User::getBirthday)
                .containsExactlyInAnyOrder(LocalDate.of(1990, 1, 1),
                        LocalDate.of(1995, 5, 5),
                        LocalDate.of(1980, 12, 4));
    }

    @Test
    public void testFindUserByExistingId() {
        User foundUser = userStorage.getUserById(1);

        assertThat(foundUser).hasFieldOrPropertyWithValue("id", 1);
        assertThat(foundUser).hasFieldOrPropertyWithValue("login", "user1");
        assertThat(foundUser).hasFieldOrPropertyWithValue("email", "user1@example.com");
        assertThat(foundUser).hasFieldOrPropertyWithValue("name", "User 1");
        assertThat(foundUser).hasFieldOrPropertyWithValue("birthday",
                LocalDate.of(1990, 1, 1));
    }

    @Test
    public void testFindUserByIncorrectId() {
        assertThatThrownBy(() -> userStorage.getUserById(100)).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testAddUser() {
        User testUser = new User();
        testUser.setLogin("testuser");
        testUser.setEmail("testuser@example.com");
        testUser.setName("Test User");
        testUser.setBirthday(LocalDate.of(2000, 11, 12));

        userStorage.addUser(testUser);

        User foundUser = userStorage.getUserById(testUser.getId());

        assertThat(foundUser).hasFieldOrPropertyWithValue("id", testUser.getId());
        assertThat(foundUser).hasFieldOrPropertyWithValue("login", "testuser");
        assertThat(foundUser).hasFieldOrPropertyWithValue("email", "testuser@example.com");
        assertThat(foundUser).hasFieldOrPropertyWithValue("name", "Test User");
        assertThat(foundUser).hasFieldOrPropertyWithValue("birthday",
                LocalDate.of(2000, 11, 12));
    }

    @Test
    public void testUpdateUser() {
        //создаём пользователя
        User testUser = new User();
        testUser.setLogin("originaluser");
        testUser.setEmail("original@example.com");
        testUser.setName("Original User");
        testUser.setBirthday(LocalDate.of(2000, 11, 12));

        userStorage.addUser(testUser);
        int userId = testUser.getId();

        //обновляем пользователя
        testUser.setLogin("updateduser");
        testUser.setEmail("updateduser@example.com");

        userStorage.updateUser(testUser);

        User foundUser = userStorage.getUserById(userId);

        assertThat(foundUser).hasFieldOrPropertyWithValue("id", testUser.getId());
        assertThat(foundUser).hasFieldOrPropertyWithValue("login", "updateduser");
        assertThat(foundUser).hasFieldOrPropertyWithValue("email", "updateduser@example.com");

    }

    @Test
    public void testDeleteUser() {
        //создаём пользователя
        User testUser = new User();
        testUser.setLogin("originaluser");
        testUser.setEmail("original@example.com");
        testUser.setName("Original User");
        testUser.setBirthday(LocalDate.of(2000, 11, 12));

        userStorage.addUser(testUser);
        int userId = testUser.getId();

        //Удаляем пользователя
        userStorage.deleteUser(userId);

        //проверяем, что на запрос по id выдаст исключение
        assertThatThrownBy(() -> userStorage.getUserById(userId)).isInstanceOf(NotFoundException.class);

        //проверяем, что id действительно нет среди пользователей
        Collection<User> remainingUsers = userStorage.getUsers();
        assertThat(remainingUsers)
                .extracting(User::getId)
                .doesNotContain(userId);
    }

    @Test
    public void testDeleteUserIncorrectId() {
        assertThatThrownBy(() -> userStorage.getUserById(100)).isInstanceOf(NotFoundException.class);

        //проверяем, что id действительно нет среди пользователей
        Collection<User> remainingUsers = userStorage.getUsers();
        assertThat(remainingUsers)
                .extracting(User::getId)
                .doesNotContain(100);
    }

    @Test
    public void testFindAllFilms() {
        Collection<Film> foundFilms = filmStorage.getFilms();

        assertThat(foundFilms).extracting(Film::getName)
                .containsExactlyInAnyOrder("film1", "film2");
        assertThat(foundFilms).extracting(Film::getDescription)
                .containsExactlyInAnyOrder("description1", "description2");
        assertThat(foundFilms).extracting(Film::getReleaseDate)
                .containsExactlyInAnyOrder(LocalDate.of(1985, 6, 2),
                        LocalDate.of(2020, 3, 12));
        assertThat(foundFilms).extracting(Film::getDuration)
                .containsExactlyInAnyOrder(200, 95);
        assertThat(foundFilms).extracting(Film::getMpa).extracting(Rating::getId)
                .containsExactlyInAnyOrder(3, 2);
    }

    @Test
    public void testFindFilmByIncorrectId() {
        assertThatThrownBy(() -> filmStorage.getFilmById(100)).isInstanceOf(NotFoundException.class);

        //проверяем, что id действительно нет среди фильмов
        Collection<Film> remainingFilms = filmStorage.getFilms();
        assertThat(remainingFilms)
                .extracting(Film::getId)
                .doesNotContain(100);
    }

    @Test
    public void testFindFilmById() {
        Film foundFilm = filmStorage.getFilmById(1);

        assertThat(foundFilm).hasFieldOrPropertyWithValue("id", 1);
        assertThat(foundFilm).hasFieldOrPropertyWithValue("name", "film1");
        assertThat(foundFilm).hasFieldOrPropertyWithValue("description", "description1");
        assertThat(foundFilm).hasFieldOrPropertyWithValue("releaseDate",
                LocalDate.of(1985, 6, 2));
        assertThat(foundFilm).hasFieldOrPropertyWithValue("duration", 200);
        assertThat(foundFilm).extracting(Film::getMpa).hasFieldOrPropertyWithValue("id", 3);
    }

    @Test
    public void testAddFilm() {
        Film testFilm = new Film();
        testFilm.setName("testfilm");
        testFilm.setDescription("testdescription");
        testFilm.setReleaseDate(LocalDate.of(2025, 1, 22));
        testFilm.setDuration(125);
        Rating rating = new Rating();
        rating.setId(5);
        testFilm.setMpa(rating);

        filmStorage.addFilm(testFilm);

        Film foundFilm = filmStorage.getFilmById(testFilm.getId());

        assertThat(foundFilm).hasFieldOrPropertyWithValue("id", testFilm.getId());
        assertThat(foundFilm).hasFieldOrPropertyWithValue("name", "testfilm");
        assertThat(foundFilm).hasFieldOrPropertyWithValue("description", "testdescription");
        assertThat(foundFilm).hasFieldOrPropertyWithValue("releaseDate",
                LocalDate.of(2025, 1, 22));
        assertThat(foundFilm).hasFieldOrPropertyWithValue("duration", 125);
        assertThat(foundFilm).extracting(Film::getMpa).hasFieldOrPropertyWithValue("id", 5);
    }

    @Test
    public void testAddFilmWithWrongRatingId() {
        Film testFilm = new Film();
        testFilm.setName("testfilm");
        testFilm.setDescription("testdescription");
        testFilm.setReleaseDate(LocalDate.of(2025, 1, 22));
        testFilm.setDuration(125);
        Rating rating = new Rating();
        rating.setId(10);
        testFilm.setMpa(rating);

        assertThatThrownBy(() -> filmStorage.addFilm(testFilm)).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testUpdateFilm() {
        //создаём фильм
        Film testFilm = new Film();
        testFilm.setName("originalfilm");
        testFilm.setDescription("originaldescription");
        testFilm.setReleaseDate(LocalDate.of(2025, 1, 22));
        testFilm.setDuration(125);
        Rating rating = new Rating();
        rating.setId(5);
        testFilm.setMpa(rating);

        filmStorage.addFilm(testFilm);
        int filmId = testFilm.getId();

        //обновляем фильм
        testFilm.setDescription("testdescription");
        testFilm.setDuration(145);

        filmStorage.updateFilm(testFilm);

        Film foundFilm = filmStorage.getFilmById(filmId);

        assertThat(foundFilm).hasFieldOrPropertyWithValue("id", testFilm.getId());
        assertThat(foundFilm).hasFieldOrPropertyWithValue("description", "testdescription");
        assertThat(foundFilm).hasFieldOrPropertyWithValue("duration", 145);
    }

    @Test
    public void testUpdateFilmWithWrongId() {
        Film testFilm = new Film();
        testFilm.setId(100);
        testFilm.setName("testfilm");
        testFilm.setDescription("testdescription");
        testFilm.setReleaseDate(LocalDate.of(2025, 1, 22));
        testFilm.setDuration(125);
        Rating rating = new Rating();
        rating.setId(5);
        testFilm.setMpa(rating);

        assertThatThrownBy(() -> filmStorage.updateFilm(testFilm)).isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testDeleteFilm() {
        filmStorage.deleteFilm(2);

        //проверяем, что на запрос по id выдаст исключение
        assertThatThrownBy(() -> filmStorage.getFilmById(2)).isInstanceOf(NotFoundException.class);

        //проверяем, что id действительно нет среди фильмов
        Collection<Film> remainingFilms = filmStorage.getFilms();
        assertThat(remainingFilms)
                .extracting(Film::getId)
                .doesNotContain(2);
    }

    @Test
    public void testDeleteFilmWithWrongId() {
        assertThatThrownBy(() -> filmStorage.deleteFilm(100)).isInstanceOf(NotFoundException.class);

        //проверяем, что id действительно нет среди фильмов
        Collection<Film> remainingFilms = filmStorage.getFilms();
        assertThat(remainingFilms)
                .extracting(Film::getId)
                .doesNotContain(100);
    }

    @Test
    public void testGetFriendsByWrongUserId() {
        assertThatThrownBy(() -> friendStorage.getFriends(100)).isInstanceOf(NotFoundException.class);

        //проверяем, что id действительно нет среди пользователей
        Collection<User> remainingUsers = userStorage.getUsers();
        assertThat(remainingUsers)
                .extracting(User::getId)
                .doesNotContain(100);
    }

    @Test
    public void testGetFriendsByUserId() {
        Collection<User> foundUsers = friendStorage.getFriends(1);

        assertThat(foundUsers).extracting(User::getLogin)
                .containsExactlyInAnyOrder("user2", "user3");
        assertThat(foundUsers).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user2@example.com", "user3@example.com");
        assertThat(foundUsers).extracting(User::getName)
                .containsExactlyInAnyOrder("User 2", "User 3");
        assertThat(foundUsers).extracting(User::getBirthday)
                .containsExactlyInAnyOrder(LocalDate.of(1995, 5, 5),
                        LocalDate.of(1980, 12, 4));
    }

    @Test
    public void testGetCommonFriends() {
        Collection<User> foundUsers = friendStorage.getCommonFriends(1, 2);

        assertThat(foundUsers).extracting(User::getLogin)
                .containsExactlyInAnyOrder("user3");
        assertThat(foundUsers).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user3@example.com");
        assertThat(foundUsers).extracting(User::getName)
                .containsExactlyInAnyOrder("User 3");
        assertThat(foundUsers).extracting(User::getBirthday)
                .containsExactlyInAnyOrder(LocalDate.of(1980, 12, 4));

    }

    @Test
    public void testGetCommonFriendsWrongUserId() {
        assertThatThrownBy(() -> friendStorage.getCommonFriends(100, 2))
                .isInstanceOf(NotFoundException.class);

        //проверяем, что id действительно нет среди пользователей
        Collection<User> remainingUsers = userStorage.getUsers();
        assertThat(remainingUsers)
                .extracting(User::getId)
                .doesNotContain(100);
    }

    @Test
    public void testGetCommonFriendsWrongOtherId() {
        assertThatThrownBy(() -> friendStorage.getCommonFriends(1, 100))
                .isInstanceOf(NotFoundException.class);

        //проверяем, что id действительно нет среди пользователей
        Collection<User> remainingUsers = userStorage.getUsers();
        assertThat(remainingUsers)
                .extracting(User::getId)
                .doesNotContain(100);
    }

    @Test
    public void testAddFriend() {
        friendStorage.addFriend(2, 1);

        //Проверяем, что теперь у пользователя два друга
        Collection<User> foundUsers = friendStorage.getFriends(2);

        assertThat(foundUsers).extracting(User::getLogin)
                .containsExactlyInAnyOrder("user1", "user3");
        assertThat(foundUsers).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@example.com", "user3@example.com");
        assertThat(foundUsers).extracting(User::getName)
                .containsExactlyInAnyOrder("User 1", "User 3");
        assertThat(foundUsers).extracting(User::getBirthday)
                .containsExactlyInAnyOrder(LocalDate.of(1990, 1, 1),
                        LocalDate.of(1980, 12, 4));
    }

    @Test
    public void testAddFriendWithWrongId() {
        assertThatThrownBy(() -> friendStorage.addFriend(1, 100))
                .isInstanceOf(NotFoundException.class);

        //проверяем, что id действительно нет среди пользователей
        Collection<User> remainingUsers = userStorage.getUsers();
        assertThat(remainingUsers)
                .extracting(User::getId)
                .doesNotContain(100);
    }

    @Test
    public void testAddFriendForWrongUserId() {
        assertThatThrownBy(() -> friendStorage.addFriend(100, 1))
                .isInstanceOf(NotFoundException.class);

        //проверяем, что id действительно нет среди пользователей
        Collection<User> remainingUsers = userStorage.getUsers();
        assertThat(remainingUsers)
                .extracting(User::getId)
                .doesNotContain(100);
    }

    @Test
    public void testDeleteFriend() {
        friendStorage.deleteFriend(1, 2);

        //Проверяем, что теперь у пользователя остался только 1 друг
        Collection<User> foundUsers = friendStorage.getFriends(1);

        assertThat(foundUsers).hasSize(1);
        assertThat(foundUsers).extracting(User::getLogin)
                .containsExactlyInAnyOrder("user3");
        assertThat(foundUsers).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user3@example.com");
        assertThat(foundUsers).extracting(User::getName)
                .containsExactlyInAnyOrder("User 3");
        assertThat(foundUsers).extracting(User::getBirthday)
                .containsExactlyInAnyOrder(LocalDate.of(1980, 12, 4));
    }

    @Test
    public void testDeleteFriendForWrongUserId() {
        assertThatThrownBy(() -> friendStorage.deleteFriend(100, 1))
                .isInstanceOf(NotFoundException.class);

        //проверяем, что id действительно нет среди пользователей
        Collection<User> remainingUsers = userStorage.getUsers();
        assertThat(remainingUsers)
                .extracting(User::getId)
                .doesNotContain(100);
    }

    @Test
    public void testDeleteFriendWithWrongId() {
        assertThatThrownBy(() -> friendStorage.deleteFriend(1, 100))
                .isInstanceOf(NotFoundException.class);

        //проверяем, что id действительно нет среди пользователей
        Collection<User> remainingUsers = userStorage.getUsers();
        assertThat(remainingUsers)
                .extracting(User::getId)
                .doesNotContain(100);
    }

    @Test
    public void testAddLikeToFilm() {
        likeStorage.likeFilm(2, 1);

        List<Integer> likes = jdbc.queryForList(
                "SELECT user_id FROM likes WHERE film_id = 2", Integer.class);
        assertThat(likes).hasSize(1);
        assertThat(likes.get(0)).isEqualTo(1);
    }

    @Test
    public void testDeleteLike() {
        //Добавление и проверка
        likeStorage.likeFilm(2, 1);
        List<Integer> likesBefore = jdbc.queryForList(
                "SELECT user_id FROM likes WHERE film_id = 2", Integer.class);
        assertThat(likesBefore).hasSize(1);
        assertThat(likesBefore.get(0)).isEqualTo(1);

        //Удаление и проверка
        likeStorage.deleteLike(2, 1);

        List<Integer> likesAfter = jdbc.queryForList(
                "SELECT user_id FROM likes WHERE film_id = 2", Integer.class);
        assertThat(likesAfter).isEmpty();
    }

    @Test
    public void testGetPopularFilms() {
        //Добавляем ещё один фильм
        Film film = new Film();
        film.setName("film3");
        film.setDescription("description3");
        film.setReleaseDate(LocalDate.of(1900, 9, 9));
        film.setDuration(45);
        Rating rating = new Rating();
        rating.setId(3);
        film.setMpa(rating);
        filmStorage.addFilm(film);

        //ставим лайки
        likeStorage.likeFilm(1, 1);
        likeStorage.likeFilm(1, 2);
        likeStorage.likeFilm(1, 3);
        likeStorage.likeFilm(2, 2);
        likeStorage.likeFilm(3, 1);
        likeStorage.likeFilm(3, 2);

        //В топ2 должны быть фильм 1 и 3
        Collection<Film> foundFilms = likeStorage.getPopularFilms(2);

        assertThat(foundFilms).extracting(Film::getName)
                .containsExactlyInAnyOrder("film1", "film3");
        assertThat(foundFilms).extracting(Film::getDescription)
                .containsExactlyInAnyOrder("description1", "description3");
        assertThat(foundFilms).extracting(Film::getReleaseDate)
                .containsExactlyInAnyOrder(LocalDate.of(1985, 6, 2),
                        LocalDate.of(1900, 9, 9));
        assertThat(foundFilms).extracting(Film::getDuration)
                .containsExactlyInAnyOrder(200, 45);
        assertThat(foundFilms).extracting(Film::getMpa).extracting(Rating::getId)
                .containsExactlyInAnyOrder(3, 3);
    }

    @Test
    public void testGetGenres() {
        Collection<Genre> genres = genreStorage.getGenres();

        assertThat(genres).extracting(Genre::getId)
                .containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6);
        assertThat(genres).extracting(Genre::getName)
                .containsExactlyInAnyOrder("Комедия", "Драма", "Мультфильм",
                        "Триллер", "Документальный", "Боевик");
    }

    @Test
    public void testGetGenreById() {
        Genre genre = genreStorage.getGenreById(2);
        assertThat(genre.getId()).isEqualTo(2);
        assertThat(genre.getName()).isEqualTo("Драма");
    }

    @Test
    public void addAndGetGenresForOneFilm() {
        Set<Integer> genreIds = Set.of(1, 3);
        genreStorage.addFilmGenres(1, genreIds);

        Set<Genre> foundGenres = genreStorage.getGenresForOneFilm(1);

        assertThat(foundGenres).extracting(Genre::getId).containsExactlyInAnyOrder(1, 3);
    }

    @Test
    public void testGetGenresForSeveralFilms() {
        //Добавляем жанры
        List<Integer> filmIds = List.of(1, 2);
        genreStorage.addFilmGenres(1, Set.of(2, 3, 4));
        genreStorage.addFilmGenres(2, Set.of(5, 2));

        Map<Integer, Set<Genre>> foundGenres = genreStorage.getGenresForAllFilms(filmIds);

        assertThat(foundGenres.keySet()).containsExactlyInAnyOrder(1, 2);

        // Проверка для фильма 1
        Set<Genre> genresForFilm1 = foundGenres.get(1);
        assertThat(genresForFilm1).extracting(Genre::getId)
                .containsExactlyInAnyOrder(2, 3, 4);
        assertThat(genresForFilm1).extracting(Genre::getName)
                .containsExactlyInAnyOrder("Драма", "Мультфильм", "Триллер");

        // Проверка для фильма 2
        Set<Genre> genresForFilm2 = foundGenres.get(2);
        assertThat(genresForFilm2).extracting(Genre::getId)
                .containsExactlyInAnyOrder(2, 5);
        assertThat(genresForFilm2).extracting(Genre::getName)
                .containsExactlyInAnyOrder("Драма", "Документальный");
    }

    @Test
    public void testGetRatings() {
        Collection<Rating> ratings = ratingStorage.getRatings();

        assertThat(ratings).extracting(Rating::getId)
                .containsExactlyInAnyOrder(1, 2, 3, 4, 5);
        assertThat(ratings).extracting(Rating::getName)
                .containsExactlyInAnyOrder("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    public void testGetRatingById() {
        Rating rating = ratingStorage.getRatingById(3);
        assertThat(rating.getId()).isEqualTo(3);
        assertThat(rating.getName()).isEqualTo("PG-13");
    }
}