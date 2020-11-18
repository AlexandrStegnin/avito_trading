ALTER TABLE trading ALTER COLUMN address TYPE varchar(1000) USING address::varchar;
