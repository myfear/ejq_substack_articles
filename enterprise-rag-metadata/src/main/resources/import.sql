INSERT INTO customerorder (id, userid, ordernumber, status, totalamount)
VALUES (nextval('customerorder_seq'), 'alice@acme.com', 'ORD-123', 'SHIPPED', 199.99);

INSERT INTO customerorder (id, userid, ordernumber, status, totalamount)
VALUES (nextval('customerorder_seq'), 'bob@acme.com', 'ORD-456', 'PENDING', 50.00);
