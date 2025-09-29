-- Seed data for Artwork table
INSERT INTO artwork (id, name, code) VALUES 
(1, 'Starry Night', 'ART-0001'),
(2, 'Mona Lisa', 'ART-0002'),
(3, 'The Persistence of Memory', 'ART-0003'),
(4, 'Girl with a Pearl Earring', 'ART-0004'),
(5, 'The Birth of Venus', 'ART-0005');

-- Seed data for ShopItem table
INSERT INTO shop_item (id, sku, title, artworkName, price, stock, description) VALUES 
(1, 'SKU-0001', 'Starry Night Print', 'Starry Night', 29.99, 50, 'Post-Impressionist masterpiece with swirling night sky.'),
(2, 'SKU-0002', 'Mona Lisa Print', 'Mona Lisa', 35.99, 30, 'Portrait renowned for enigmatic expression.'),
(3, 'SKU-0003', 'Persistence of Memory Print', 'The Persistence of Memory', 32.99, 25, 'Surreal landscape with melting clocks.'),
(4, 'SKU-0004', 'Girl with Pearl Earring Print', 'Girl with a Pearl Earring', 28.99, 40, 'Tronie with luminous light and gaze.'),
(5, 'SKU-0005', 'Birth of Venus Print', 'The Birth of Venus', 38.99, 35, 'Renaissance masterpiece depicting Venus emerging from the sea.');
