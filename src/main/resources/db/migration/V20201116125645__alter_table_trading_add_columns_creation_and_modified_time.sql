ALTER TABLE trading_sber ADD creation_time TIMESTAMP NOT NULL DEFAULT NOW();
ALTER TABLE trading_sber ADD modified_time TIMESTAMP DEFAULT NULL;
