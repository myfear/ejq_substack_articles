-- Customers
INSERT INTO customer (id, fullName, street, postalCode, city, countryCode, iban, bic, email)
VALUES 
  (1, 'Max Mustermann', 'Beispielstraße 1', '80331', 'München', 'DE', 'DE44500105175407324931', 'INGDDEFFXXX', 'max@example.org'),
  (2, 'Erika Musterfrau', 'Hauptstraße 5', '50667', 'Köln', 'DE', 'DE88500105174607324932', 'INGDDEFFXXX', 'erika@example.org');

-- Vehicles
INSERT INTO vehicle (id, vin, registration, typklasse, regionalklasse)
VALUES
  (1, 'VIN001', 'M-1001', 'SPORT', 'BY'),
  (2, 'VIN002', 'K-2001', 'STD', 'NW');

-- Policies
INSERT INTO policy (id, customer_id, vehicle_id, coverage, bundesland, validFrom, validTo, baseAnnualPremium, cancelled)
VALUES
  (1, 
   (SELECT id FROM customer WHERE email='max@example.org'),
   (SELECT id FROM vehicle WHERE vin='VIN001'),
   'VK', 'BY', '2024-01-01', '2026-12-31', 620.00, false),
  (2,
   (SELECT id FROM customer WHERE email='erika@example.org'),
   (SELECT id FROM vehicle WHERE vin='VIN002'),
   'HP', 'NW', '2024-01-01', '2026-12-31', 480.00, false);

-- Claims history
INSERT INTO claimshistory (id, customer_id, yearsNoClaim, claimsCountLastYear, updatedAt)
VALUES
  (1,
   (SELECT id FROM customer WHERE email='max@example.org'),
   5, 0, current_date),
  (2,
   (SELECT id FROM customer WHERE email='erika@example.org'),
   2, 1, current_date);