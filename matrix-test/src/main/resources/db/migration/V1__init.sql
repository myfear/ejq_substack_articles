create table Person (id bigint not null, name varchar(255), primary key (id));
create sequence Person_SEQ start with 2 increment by 50;
INSERT INTO Person (id,name) VALUES (1,'Neo');