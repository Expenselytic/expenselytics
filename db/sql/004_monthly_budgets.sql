-- Apply once after 003_spending_experiments.sql.
CREATE TABLE monthly_budget (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    month VARCHAR(7) NOT NULL,
    category VARCHAR(255) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    CONSTRAINT uq_monthly_budget UNIQUE (user_id, month, category)
);
