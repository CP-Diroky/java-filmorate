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

CREATE TABLE IF NOT EXISTS directors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
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

CREATE TABLE IF NOT EXISTS film_directors (
    film_id  BIGINT,
    director_id BIGINT,
    FOREIGN KEY (film_id) REFERENCES films (id),
    FOREIGN KEY (director_id) REFERENCES directors (id),
    UNIQUE (film_id, director_id)
);

CREATE TABLE IF NOT EXISTS friends (
    user_id   BIGINT,
    friend_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (friend_id) REFERENCES users (id),
    UNIQUE (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(250) NOT NULL,
    is_positive BOOLEAN NOT NULL,
    user_id BIGINT NOT NULL,
    film_id BIGINT NOT NULL,
    useful BIGINT NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (film_id) REFERENCES films (id)
);

CREATE TABLE IF NOT EXISTS review_likes (
    review_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_like BOOLEAN NOT NULL, -- true = like, false = dislike
    PRIMARY KEY (review_id, user_id),
    FOREIGN KEY (review_id) REFERENCES reviews (id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (id)
    );
