-- This file allow to write SQL commands that will be emitted in test and dev.
-- The commands are commented as their support depends of the database
-- insert into myentity (id, field) values(1, 'field-1');
-- insert into myentity (id, field) values(2, 'field-2');
-- insert into myentity (id, field) values(3, 'field-3');
-- alter sequence myentity_seq restart with 4;

insert into products (id, name, price, stockQuantity) values(1, 'Laptop', 999.99, 10);
insert into products (id, name, price, stockQuantity) values(2, 'Mouse', 29.99, 50);
insert into products (id, name, price, stockQuantity) values(3, 'Keyboard', 79.99, 25);
insert into products (id, name, price, stockQuantity) values(4, 'Monitor', 299.99, 5);
alter sequence products_seq restart with 5;