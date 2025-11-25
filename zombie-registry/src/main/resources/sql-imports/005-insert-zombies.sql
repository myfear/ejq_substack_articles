-- Know your enemy

INSERT INTO zombies (id, type, speed_level, intelligence_level, last_spotted_zone, threat_level) VALUES
(1, 'Walker', 2, 1, 'Downtown District', 'LOW'),
(2, 'Runner', 8, 1, 'Shopping Mall', 'HIGH'),
(3, 'Shambler', 1, 1, 'Suburbs', 'LOW'),
(4, 'Screamer', 4, 2, 'Hospital', 'MEDIUM'),
(5, 'Tank', 3, 1, 'Military Base', 'RUN!!!'),
(6, 'Spitter', 5, 3, 'Chemical Plant', 'HIGH'),
(7, 'Crawler', 2, 1, 'Sewers', 'LOW'),
(8, 'Horde Leader', 6, 5, 'City Center', 'RUN!!!'),
(9, 'Bloater', 2, 1, 'Gas Station', 'MEDIUM'),
(10, 'Fresh Turn', 4, 2, 'Residential Area', 'MEDIUM');

SELECT setval('zombies_SEQ', COALESCE((SELECT MAX(id) FROM zombies), 1));