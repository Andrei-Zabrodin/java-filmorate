INSERT INTO users (email, login, name, birthday) VALUES
    ('user1@example.com', 'user1', 'User 1', '1990-01-01'),
    ('user2@example.com', 'user2', 'User 2', '1995-05-05'),
    ('user3@example.com', 'user3', 'User 3', '1980-12-04');

INSERT INTO films (name, description, release_date, duration, rating_id) VALUES
    ('film1', 'description1', '1985-06-02', 200, 3),
    ('film2', 'description2', '2020-03-12', 95, 2);

INSERT INTO friends (user_id, friend_id) VALUES
    (1, 2),
    (1, 3),
    (2, 3),
    (3, 1);

MERGE INTO genres (name) KEY (name) VALUES
    ('Комедия'),
    ('Драма'),
    ('Мультфильм'),
    ('Триллер'),
    ('Документальный'),
    ('Боевик');

MERGE INTO ratings (name) KEY (name) VALUES
    ('G'),
    ('PG'),
    ('PG-13'),
    ('R'),
    ('NC-17');