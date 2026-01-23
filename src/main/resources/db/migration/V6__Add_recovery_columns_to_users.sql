ALTER TABLE tb_users ADD COLUMN recovery_code VARCHAR(10);
ALTER TABLE tb_users ADD COLUMN recovery_code_expiration TIMESTAMP;