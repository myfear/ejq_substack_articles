-- Brands
INSERT INTO brand(id, name) VALUES (1, 'Volkswagen');
INSERT INTO brand(id, name) VALUES (2, 'BMW');
INSERT INTO brand(id, name) VALUES (3, 'Mercedes-Benz');
INSERT INTO brand(id, name) VALUES (4, 'Audi');

-- Dealerships
INSERT INTO dealership(id, name, city) VALUES (1, 'Auto-Haus München', 'Munich');
INSERT INTO dealership(id, name, city) VALUES (2, 'Premium Cars Berlin', 'Berlin');
INSERT INTO dealership(id, name, city) VALUES (3, 'Süd-West Automobile', 'Stuttgart');

-- Cars
-- ID, COLOR, MODEL, PRICE, PROD_YEAR, BRAND_ID, DEALER_ID
INSERT INTO car(id, color, model, price, productionYear, brand_id, dealership_id) VALUES (101, 'Black', 'Golf', 25000.00, 2022, 1, 1);
INSERT INTO car(id, color, model, price, productionYear, brand_id, dealership_id) VALUES (102, 'White', '3 Series', 45000.00, 2023, 2, 2);
INSERT INTO car(id, color, model, price, productionYear, brand_id, dealership_id) VALUES (103, 'Silver', 'C-Class', 48000.00, 2023, 3, 3);
INSERT INTO car(id, color, model, price, productionYear, brand_id, dealership_id) VALUES (104, 'Red', 'A4', 42000.00, 2022, 4, 1);
INSERT INTO car(id, color, model, price, productionYear, brand_id, dealership_id) VALUES (105, 'Black', 'Tiguan', 32000.00, 2021, 1, 2);
INSERT INTO car(id, color, model, price, productionYear, brand_id, dealership_id) VALUES (106, 'Blue', 'X5', 75000.00, 2024, 2, 3);
INSERT INTO car(id, color, model, price, productionYear, brand_id, dealership_id) VALUES (107, 'White', 'A6', 65000.00, 2024, 4, 2);

-- Car Features
-- CAR_ID, FEATURE
INSERT INTO car_features(car_id, feature) VALUES (101, 'Sunroof');
INSERT INTO car_features(car_id, feature) VALUES (101, 'Heated Seats');
INSERT INTO car_features(car_id, feature) VALUES (102, 'Sunroof');
INSERT INTO car_features(car_id, feature) VALUES (102, 'Sport Package');
INSERT INTO car_features(car_id, feature) VALUES (103, 'Heated Seats');
INSERT INTO car_features(car_id, feature) VALUES (104, 'LED Headlights');
INSERT INTO car_features(car_id, feature) VALUES (105, 'Sunroof');
INSERT INTO car_features(car_id, feature) VALUES (106, 'Sport Package');
INSERT INTO car_features(car_id, feature) VALUES (106, 'Heated Seats');
INSERT INTO car_features(car_id, feature) VALUES (107, 'LED Headlights');