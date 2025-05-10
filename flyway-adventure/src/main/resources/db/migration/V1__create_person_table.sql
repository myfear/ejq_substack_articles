CREATE TABLE person (
    id BIGINT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL
);

create sequence Person_SEQ start with 1 increment by 50;