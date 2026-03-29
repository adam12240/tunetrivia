CREATE TABLE play_stat (
                           id BIGSERIAL PRIMARY KEY,
                           artist VARCHAR(255),
                           correct BOOLEAN,
                           genre VARCHAR(255),
                           played_at TIMESTAMP WITH TIME ZONE,
                           title VARCHAR(255),
                           track_id VARCHAR(255),
                           user_id VARCHAR(255)
);