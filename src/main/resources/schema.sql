CREATE TABLE IF NOT EXISTS users (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    email    VARCHAR(255),
    login    VARCHAR(255),
    name     VARCHAR(255),
    birthday DATE
);

CREATE TABLE IF NOT EXISTS mpa (
    id   BIGINT PRIMARY KEY,
    name VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS genres (
    id   BIGINT PRIMARY KEY,
    name VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS films (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255),
    description  VARCHAR(200),
    release_date DATE,
    duration     INT,
    mpa_id       BIGINT,
    FOREIGN KEY (mpa_id) REFERENCES mpa (id)
);

CREATE TABLE IF NOT EXISTS film_likes (
    film_id BIGINT,
    user_id BIGINT,
    FOREIGN KEY (film_id) REFERENCES films (id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    UNIQUE (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS film_genres (
    film_id  BIGINT,
    genre_id BIGINT,
    FOREIGN KEY (film_id) REFERENCES films (id),
    FOREIGN KEY (genre_id) REFERENCES genres (id),
    UNIQUE (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS friends (
    user_id   BIGINT,
    friend_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (friend_id) REFERENCES users (id),
    UNIQUE (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT REFERENCES users (id),
    timestamp BIGINT NOT NULL,
    event_type varchar NOT NULL CHECK (event_type IN ('LIKE', 'REVIEW', 'FRIEND')),
    operation varchar NOT NULL CHECK (operation IN ('ADD', 'REMOVE', 'UPDATE')),
    entity_id BIGINT
);