-- Create replication user
CREATE USER replicator REPLICATION LOGIN CONNECTION LIMIT 100 ENCRYPTED PASSWORD 'replicator123';
SELECT * FROM pg_create_physical_replication_slot('replication_slot');

-- Grant permissions for replicator user to access tables (needed for Hibernate validation)
GRANT CONNECT ON DATABASE ecommerce TO replicator;
GRANT USAGE ON SCHEMA public TO replicator;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO replicator;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA public TO replicator;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO replicator;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON SEQUENCES TO replicator;

-- Domain tables
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    stock_quantity INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    product_id BIGINT REFERENCES products(id),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL
);

-- Create sequences for Hibernate
CREATE SEQUENCE order_items_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE orders_SEQ START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE products_SEQ START WITH 1 INCREMENT BY 50;


-- Set the sequences to start after the existing data
SELECT setval('products_SEQ', (SELECT COALESCE(MAX(id), 0) + 1 FROM products));
SELECT setval('orders_SEQ', (SELECT COALESCE(MAX(id), 0) + 1 FROM orders));
SELECT setval('order_items_SEQ', (SELECT COALESCE(MAX(id), 0) + 1 FROM order_items));

-- Seed data
INSERT INTO products (name, price, description, category, stock_quantity) VALUES
('Laptop Pro', 1299.99, 'High-performance laptop', 'Electronics', 50),
('Wireless Mouse', 29.99, 'Bluetooth wireless mouse', 'Electronics', 200),
('Office Chair', 199.99, 'Ergonomic office chair', 'Furniture', 30);

INSERT INTO orders (customer_name, total_amount, status) VALUES
('John Doe', 1329.98, 'COMPLETED'),
('Jane Smith', 199.99, 'PENDING');

INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(1, 1, 1, 1299.99),
(1, 2, 1, 29.99),
(2, 3, 1, 199.99);
