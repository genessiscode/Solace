-- ============================================================
-- EcoNest - demo data. Never loaded by the application.
--
-- Apply AFTER schema.sql:
--   mysql -u root econest_db < src/main/resources/db/seed.sql
--
-- Delete this file's contents before submission if a clean
-- database is preferred; the app creates all rows itself.
-- ============================================================

-- ---------- products ----------
INSERT INTO products
  (product_id, name, category, price, eco_rating, description,
   sunlight, pet_safe, material, diameter_cm, difficulty)
VALUES
  ('P001', 'Snake Plant premium', 'PLANT',   2500.00, 5,
   'Hardy indoor air purifier',            'LOW',    FALSE, NULL, NULL, NULL),
  ('P002', 'Aloe Vera',           'PLANT',   1200.00, 4,
   'Succulent with soothing gel',          'HIGH',   FALSE, NULL, NULL, NULL),
  ('P003', 'Peace Lily',          'PLANT',   3200.00, 4,
   'Flowering shade lover',                'LOW',    FALSE, NULL, NULL, NULL),
  ('P004', 'Spider Plant',        'PLANT',    950.00, 5,
   'Pet-safe trailing favourite',          'MEDIUM', TRUE,  NULL, NULL, NULL),
  ('P005', 'Terracotta Pot 20cm', 'PLANTER', 1450.00, 5,
   'Breathable natural clay',              NULL, NULL, 'TERRACOTTA', 20, NULL),
  ('P006', 'Ceramic Pot 15cm',    'PLANTER', 1900.00, 4,
   'Glazed finish, drainage hole',         NULL, NULL, 'CERAMIC',    15, NULL),
  ('P007', 'Recycled Plastic Pot','PLANTER',  650.00, 5,
   'Made from reclaimed plastic',          NULL, NULL, 'RECYCLED',   18, NULL),
  ('P008', 'Balcony Herb Kit',    'KIT',     4800.00, 5,
   'Basil, mint and coriander starter',    NULL, NULL, NULL, NULL, 'BEGINNER'),
  ('P009', 'Salad Greens Kit',    'KIT',     5200.00, 4,
   'Lettuce and rocket for small spaces',  NULL, NULL, NULL, NULL, 'INTERMEDIATE'),
  ('P010', 'Bonsai Starter Kit',  'KIT',     7500.00, 3,
   'Ficus with tools and guide',           NULL, NULL, NULL, NULL, 'ADVANCED');

-- ---------- inventory ----------
-- P001 carries forward the single row from the original econest_db.sql.
INSERT INTO inventory (product_id, quantity, reorder_level, supplier_name) VALUES
  ('P001', 15,  5, 'GreenLeaf Supplies'),
  ('P002', 40, 10, 'GreenLeaf Supplies'),
  ('P003',  8, 10, 'Ceylon Nursery'),
  ('P004', 60, 15, 'Ceylon Nursery'),
  ('P005', 25,  8, 'Clay Works Lanka'),
  ('P006', 12,  6, 'Clay Works Lanka'),
  ('P007', 90, 20, 'EcoPlast'),
  ('P008',  6,  5, 'GrowKit Lanka'),
  ('P009',  3,  5, 'GrowKit Lanka'),
  ('P010',  2,  3, 'GrowKit Lanka');

-- ---------- clients ----------
INSERT INTO clients
  (client_id, name, client_type, address, phone, email, company_name)
VALUES
  ('C001', 'Nimal Perera',      'HOUSEHOLD', '12 Temple Road, Colombo 05',
   '0771234567', 'nimal@example.com',  NULL),
  ('C002', 'Ayasha Fernando',   'HOUSEHOLD', '88 Lake Drive, Kandy',
   '0712345678', 'ayasha@example.com', NULL),
  ('C003', 'Orion Software Ltd','OFFICE',    'Level 4, Orion Tower, Colombo 03',
   '0112345678', 'ops@orion.example',  'Orion Software Ltd'),
  ('C004', 'Blue Lagoon Hotel', 'OFFICE',    '45 Beach Road, Galle',
   '0912345678', 'facilities@bluelagoon.example', 'Blue Lagoon Hotel'),
  ('C005', 'Ravi Silva',        'HOUSEHOLD', '3 Hill Street, Negombo',
   '0765432109', 'ravi@example.com',   NULL);

-- ---------- agents and vehicles ----------
INSERT INTO agents (agent_id, name, phone, email, status) VALUES
  ('A001', 'Kasun Bandara',  '0751112223', 'kasun@econest.example',  'AVAILABLE'),
  ('A002', 'Dilani Jayawardena', '0752223334', 'dilani@econest.example', 'AVAILABLE'),
  ('A003', 'Mohamed Rizwan', '0753334445', 'rizwan@econest.example', 'ON_DELIVERY'),
  ('A004', 'Sanduni Rathnayake', '0754445556', 'sanduni@econest.example', 'INACTIVE');

INSERT INTO vehicles (vehicle_id, agent_id, type, plate_number, capacity_kg) VALUES
  ('V001', 'A001', 'VAN',       'CAB-1234', 800.00),
  ('V002', 'A002', 'MOTORBIKE', 'BGH-5678',  40.00),
  ('V003', 'A003', 'LORRY',     'WP-9012', 2500.00),
  ('V004', 'A004', 'CAR',       'KL-3456',  300.00);

-- ---------- orders and line items ----------
INSERT INTO orders (order_id, client_id, order_date, status, total_amount) VALUES
  ('O001', 'C001', '2026-09-05', 'DELIVERED',  6700.00),
  ('O002', 'C003', '2026-09-18', 'DISPATCHED', 14400.00),
  ('O003', 'C002', '2026-10-02', 'CONFIRMED',   2150.00),
  ('O004', 'C005', '2026-10-06', 'PENDING',     7500.00);

INSERT INTO order_items
  (order_id, product_id, quantity, unit_price, line_total)
VALUES
  ('O001', 'P001', 1, 2500.00,  2500.00),
  ('O001', 'P002', 1, 1200.00,  1200.00),
  ('O001', 'P005', 1, 1450.00,  1450.00),
  ('O001', 'P007', 1,  650.00,   650.00),
  ('O001', 'P008', 1, 4800.00,  4800.00),
  ('O002', 'P001', 2, 2500.00,  5000.00),
  ('O002', 'P008', 1, 4800.00,  4800.00),
  ('O002', 'P009', 1, 5200.00,  5200.00),
  ('O003', 'P002', 1, 1200.00,  1200.00),
  ('O003', 'P004', 1,  950.00,   950.00),
  ('O004', 'P010', 1, 7500.00,  7500.00);

-- ---------- deliveries ----------
INSERT INTO deliveries
  (delivery_id, order_id, agent_id, scheduled_date, status, delivered_at)
VALUES
  ('D001', 'O001', 'A001', '2026-09-07', 'DELIVERED',  '2026-09-07 14:20:00'),
  ('D002', 'O002', 'A003', '2026-09-20', 'IN_TRANSIT', NULL);