package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;
import java.util.Optional;

@Repository
@Slf4j
public class RatingDbStorage extends DbStorage<Rating> implements RatingStorage {
    private static final String GET_RATINGS_QUERY = "SELECT * FROM ratings";
    private static final String GET_RATING_BY_ID_QUERY = "SELECT * FROM ratings WHERE rating_id = ?";

    public RatingDbStorage(JdbcTemplate jdbc, RowMapper<Rating> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Rating> getRatings() {
        return findMany(GET_RATINGS_QUERY);
    }

    @Override
    public Rating getRatingById(int id) {
        Optional<Rating> rating = findOne(GET_RATING_BY_ID_QUERY, id);
        if (rating.isPresent()) {
            log.debug("Нашли рейтинг с id {}", id);
            return rating.get();
        } else {
            log.debug("Не удалось найти рейтинг с указанным id");
            throw new NotFoundException("Рейтинг с id " + id + " не найден");
        }
    }
}