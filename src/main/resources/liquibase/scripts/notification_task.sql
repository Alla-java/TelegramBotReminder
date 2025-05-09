-- liquibase formatted sql

-- changeset azatcepina:1
CREATE TABLE notification_task (
    id SERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    notification_text TEXT NOT NULL,
    notification_datetime TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) DEFAULT 'PENDING'
);