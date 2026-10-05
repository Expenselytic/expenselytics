-- MySQL 8: apply once after 001_accounts.sql, before starting the updated
-- app.
ALTER TABLE `user` ADD COLUMN last_login DATETIME(6) NULL;
CREATE TABLE saving (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    amount VARCHAR(255) NOT NULL,
    date DATETIME(6) NOT NULL,
    uid INT NOT NULL
);
