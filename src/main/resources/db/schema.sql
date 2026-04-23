CREATE TABLE IF NOT EXISTS attach (
    attach_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attach_type VARCHAR(30) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    stored_file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(500) NOT NULL,
    nickname VARCHAR(30) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS events (
    event_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_name VARCHAR(100) NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    representative_attach_id BIGINT,
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    winner_limit INT NOT NULL,
    winner_count INT NOT NULL,
    participant_count INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_by BIGINT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_events_representative_attach
        FOREIGN KEY (representative_attach_id) REFERENCES attach (attach_id),
    CONSTRAINT fk_events_created_by
        FOREIGN KEY (created_by) REFERENCES users (user_id)
);

CREATE TABLE IF NOT EXISTS event_participations (
    participation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    participated_at TIMESTAMP NOT NULL,
    request_sequence BIGINT NOT NULL,
    result_status VARCHAR(20) NOT NULL,
    gifticon_attach_id BIGINT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_event_participations_event
        FOREIGN KEY (event_id) REFERENCES events (event_id),
    CONSTRAINT fk_event_participations_user
        FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_event_participations_gifticon_attach
        FOREIGN KEY (gifticon_attach_id) REFERENCES attach (attach_id),
    CONSTRAINT uk_event_participations_event_user UNIQUE (event_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_events_status_start_at
    ON events (status, start_at);

CREATE INDEX IF NOT EXISTS idx_event_participations_event_sequence
    ON event_participations (event_id, request_sequence);

CREATE INDEX IF NOT EXISTS idx_event_participations_user_created_at
    ON event_participations (user_id, created_at);

CREATE INDEX IF NOT EXISTS idx_event_participations_event_result_sequence
    ON event_participations (event_id, result_status, request_sequence);
