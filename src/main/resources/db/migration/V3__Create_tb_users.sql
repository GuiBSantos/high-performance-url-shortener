-- 1. Garante que começamos do zero (Dropa a tabela antiga se existir)
DROP TABLE IF EXISTS tb_users CASCADE;

CREATE TABLE tb_users (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          username VARCHAR(255) NOT NULL UNIQUE,
                          password VARCHAR(255) NOT NULL,
                          avatar_url VARCHAR(255),
                          role VARCHAR(50) DEFAULT 'USER',
                          created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
                          updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

DROP TABLE IF EXISTS urls CASCADE;

CREATE TABLE urls (
                      id BIGSERIAL PRIMARY KEY,
                      original_url TEXT NOT NULL,
                      short_code VARCHAR(10) NOT NULL UNIQUE,
                      created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
                      updated_at TIMESTAMP WITHOUT TIME ZONE,
                      expires_at TIMESTAMP WITHOUT TIME ZONE,
                      access_count BIGINT DEFAULT 0,
                      access_limit BIGINT,
                      user_id UUID,
                      CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE CASCADE
);

CREATE INDEX idx_urls_short_code ON urls(short_code);