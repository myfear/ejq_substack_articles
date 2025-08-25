-- Populate the Transaction and Transaction_items tables
-- Hibernate creates a join table 'Transaction_items' for the @ElementCollection
INSERT INTO Transaction(id) VALUES (1), (2), (3), (4), (5), (6), (7), (8), (9), (10), (11), (12), (13), (14), (15), (16), (17), (18), (19), (20), (21), (22), (23), (24), (25);

INSERT INTO Transaction_items(Transaction_id, items) VALUES (1, 'whole milk'), (1, 'rolls/buns'), (1, 'yogurt');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (2, 'diapers'), (2, 'canned beer'), (2, 'napkins'), (2, 'sausage');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (3, 'citrus fruit'), (3, 'pastry'), (3, 'soda');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (4, 'whole milk'), (4, 'butter'), (4, 'rolls/buns');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (5, 'bottled water'), (5, 'yogurt');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (6, 'sausage'), (6, 'rolls/buns'), (6, 'mustard');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (7, 'diapers'), (7, 'canned beer'), (7, 'shopping bags');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (8, 'whole milk'), (8, 'yogurt'), (8, 'tropical fruit');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (9, 'brown bread'), (9, 'butter');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (10, 'newspapers');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (11, 'diapers'), (11, 'canned beer');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (12, 'root vegetables'), (12, 'onions'), (12, 'sausage');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (13, 'whole milk'), (13, 'rolls/buns');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (14, 'soda'), (14, 'bottled water');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (15, 'diapers'), (15, 'canned beer'), (15, 'whole milk');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (16, 'pastry'), (16, 'coffee');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (17, 'sausage'), (17, 'rolls/buns');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (18, 'yogurt'), (18, 'citrus fruit');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (19, 'whole milk'), (19, 'butter'), (19, 'pastry');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (20, 'diapers'), (20, 'canned beer');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (21, 'whole milk'), (21, 'root vegetables'), (21, 'yogurt');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (22, 'soda');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (23, 'sausage'), (23, 'brown bread');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (24, 'diapers'), (24, 'napkins');
INSERT INTO Transaction_items(Transaction_id, items) VALUES (25, 'whole milk'), (25, 'rolls/buns');