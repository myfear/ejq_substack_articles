CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS geoname (
    id INT PRIMARY KEY,
    name VARCHAR(200),
    asciiname VARCHAR(200),
    alternatenames TEXT,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    feature_class CHAR(1),
    feature_code VARCHAR(10),
    country_code CHAR(2),
    cc2 VARCHAR(200),
    admin1_code VARCHAR(20),
    admin2_code VARCHAR(80),
    admin3_code VARCHAR(20),
    admin4_code VARCHAR(20),
    population BIGINT,
    elevation INT,
    dem INT,
    timezone VARCHAR(40),
    modification_date DATE
);

CREATE INDEX IF NOT EXISTS idx_gin_geoname_asciiname ON geoname USING GIN (asciiname gin_trgm_ops);