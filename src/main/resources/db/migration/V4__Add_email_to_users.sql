ALTER TABLE tb_users ADD COLUMN email VARCHAR(255);

UPDATE tb_users SET email = username || '@temporary.com' WHERE email IS NULL;

ALTER TABLE tb_users ALTER COLUMN email SET NOT NULL;

ALTER TABLE tb_users ADD CONSTRAINT uq_users_email UNIQUE (email);