-- Sample data for OrderEntity table
INSERT INTO OrderEntity (id,customer, item, quantity) VALUES ('1','John Doe', 'Laptop', 1);
INSERT INTO OrderEntity (id,customer, item, quantity) VALUES ('2','Jane Smith', 'Smartphone', 2);
INSERT INTO OrderEntity (id,customer, item, quantity) VALUES ('3','Robert Johnson', 'Headphones', 3);
INSERT INTO OrderEntity (id,customer, item, quantity) VALUES ('4','Emily Davis', 'Monitor', 1);
INSERT INTO OrderEntity (id,customer, item, quantity) VALUES ('5','Michael Brown', 'Keyboard', 2);
INSERT INTO OrderEntity (id,customer, item, quantity) VALUES ('6','Sarah Wilson', 'Mouse', 5);
INSERT INTO OrderEntity (id,customer, item, quantity) VALUES ('7','David Miller', 'Printer', 1);
INSERT INTO OrderEntity (id,customer, item, quantity) VALUES ('8','Jennifer Taylor', 'External Hard Drive', 2);
INSERT INTO OrderEntity (id,customer, item, quantity) VALUES ('9','Thomas Anderson', 'USB Cable', 10);
INSERT INTO OrderEntity (id,customer, item, quantity) VALUES ('10','Lisa Garcia', 'Webcam', 1);
ALTER SEQUENCE orderentity_seq RESTART WITH 20;