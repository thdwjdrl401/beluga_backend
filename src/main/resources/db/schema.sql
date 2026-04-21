CREATE TABLE IF NOT EXISTS events (
    event_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    winner_limit INT NOT NULL,
    current_winner_count INT NOT NULL,
    last_request_sequence BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS event_participations (
    participation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    participated_at TIMESTAMP NOT NULL,
    result_status VARCHAR(20) NOT NULL,
    request_sequence BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_event_participations_event
        FOREIGN KEY (event_id) REFERENCES events (event_id),
    CONSTRAINT fk_event_participations_user
        FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT uk_event_participations_event_user UNIQUE (event_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_events_status_start_at
    ON events (status, start_at);

CREATE INDEX IF NOT EXISTS idx_event_participations_event_sequence
    ON event_participations (event_id, request_sequence);

CREATE INDEX IF NOT EXISTS idx_event_participations_user_event
    ON event_participations (user_id, event_id);

CREATE INDEX IF NOT EXISTS idx_event_participations_event_result
    ON event_participations (event_id, result_status);
