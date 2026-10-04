-- MySQL 8. Apply once to an existing application database before startup.
CREATE TABLE `user` (
    user_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(512) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT uq_user_email UNIQUE (email)
);
CREATE TABLE user_device_info (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(1024),
    device_type VARCHAR(16) NOT NULL,
    mobile BIT,
    state VARCHAR(255),
    country VARCHAR(255),
    event_type VARCHAR(16) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    INDEX idx_device_user_created (user_id, created_at),
    INDEX idx_device_created (created_at),
    CONSTRAINT fk_device_user FOREIGN KEY (user_id) REFERENCES `user` (user_id)
);
CREATE TABLE user_session (
    token_hash VARCHAR(64) NOT NULL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    INDEX idx_session_expiry (expires_at),
    INDEX idx_session_user (user_id),
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES `user` (user_id)
);
