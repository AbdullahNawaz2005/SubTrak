-- Run this manually in MySQL if you prefer explicit table creation
-- Otherwise Hibernate will auto-create with ddl-auto=update

CREATE TABLE IF NOT EXISTS users (
    id                   VARCHAR(36)     PRIMARY KEY,
    name                 VARCHAR(60)     NOT NULL,
    email                VARCHAR(255)    NOT NULL UNIQUE,
    password_hash        VARCHAR(255)    NOT NULL,
    locale               VARCHAR(10)     NOT NULL DEFAULT 'en',
    display_currency     VARCHAR(10)     NOT NULL DEFAULT 'USD',
    salary               DECIMAL(15, 2)  NOT NULL DEFAULT 0,
    salary_currency      VARCHAR(10)     NOT NULL DEFAULT 'USD',
    budget_limit_percent DECIMAL(5, 2)   NOT NULL DEFAULT 20,
    created_at           TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          VARCHAR(36)     PRIMARY KEY,
    user_id     VARCHAR(36)     NOT NULL,
    token_hash  VARCHAR(64)     NOT NULL UNIQUE,
    expires_at  TIMESTAMP       NOT NULL,
    revoked     TINYINT(1)      NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_hash ON refresh_tokens(token_hash);
CREATE INDEX idx_users_email ON users(email);
