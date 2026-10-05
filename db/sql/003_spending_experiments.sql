-- Apply once after 002_savings_last_login.sql.
CREATE TABLE spending_experiment (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    duration_days INT NOT NULL
);
CREATE TABLE experiment_saving (
    saving_id BIGINT NOT NULL PRIMARY KEY,
    experiment_id BIGINT NOT NULL,
    CONSTRAINT fk_experiment_saving FOREIGN KEY (saving_id) REFERENCES saving(id) ON DELETE CASCADE,
    CONSTRAINT fk_saving_experiment FOREIGN KEY (experiment_id) REFERENCES spending_experiment(id)
);
