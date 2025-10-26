-- Critical supply locations

INSERT INTO supply_caches (id, location_name, latitude, longitude, food_units, water_units, medical_supplies, ammunition_count, is_compromised) VALUES
(1, 'Abandoned Costco', 34.0522, -118.2437, 500, 1000, 50, 200, false),
(2, 'Police Station Armory', 34.0589, -118.2456, 20, 50, 100, 5000, false),
(3, 'Rooftop Garden Alpha', 34.0612, -118.2512, 200, 100, 10, 0, false),
(4, 'Pharmacy on 5th', 34.0534, -118.2398, 10, 20, 500, 50, false),
(5, 'Fort Deadwood', 34.0678, -118.2601, 1000, 2000, 200, 10000, false),
(6, 'Overrun Walmart', 34.0445, -118.2334, 300, 200, 30, 100, true),
(7, 'Underground Bunker 7', 34.0712, -118.2645, 5000, 8000, 1000, 50000, false),
(8, 'Gas N Go', 34.0489, -118.2267, 50, 100, 5, 500, true);