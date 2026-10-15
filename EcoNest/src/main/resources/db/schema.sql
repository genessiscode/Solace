SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS deliveries;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS vehicles;
DROP TABLE IF EXISTS agents;
DROP TABLE IF EXISTS clients;
DROP TABLE IF EXISTS inventory;
DROP TABLE IF EXISTS products;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE products (
  product_id   VARCHAR(20)   NOT NULL,
  name         VARCHAR(100)  NOT NULL,
  category     ENUM('PLANT','PLANTER','KIT') NOT NULL,
  price        DECIMAL(10,2) NOT NULL,
  eco_rating   TINYINT       NOT NULL,
  description  VARCHAR(255)  DEFAULT NULL,
  sunlight     VARCHAR(20)   DEFAULT NULL,
  pet_safe     BOOLEAN       DEFAULT NULL,
  material     VARCHAR(30)   DEFAULT NULL,
  diameter_cm  INT           DEFAULT NULL,
  difficulty   VARCHAR(20)   DEFAULT NULL,
  created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (product_id),
  KEY idx_products_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE inventory (
  product_id     VARCHAR(20) NOT NULL,
  quantity       INT         NOT NULL DEFAULT 0,
  reorder_level  INT         NOT NULL DEFAULT 0,
  supplier_name  VARCHAR(100) DEFAULT NULL,
  updated_at     TIMESTAMP   NOT NULL
                 DEFAULT CURRENT_TIMESTAMP
                 ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (product_id),
  CONSTRAINT fk_inventory_product
    FOREIGN KEY (product_id) REFERENCES products (product_id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE clients (
  client_id    VARCHAR(20)  NOT NULL,
  name         VARCHAR(100) NOT NULL,
  client_type  ENUM('HOUSEHOLD','OFFICE') NOT NULL,
  address      VARCHAR(255) DEFAULT NULL,
  phone        VARCHAR(20)  DEFAULT NULL,
  email        VARCHAR(100) DEFAULT NULL,
  company_name VARCHAR(100) DEFAULT NULL,
  created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (client_id),
  KEY idx_clients_type (client_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE agents (
  agent_id   VARCHAR(20)  NOT NULL,
  name       VARCHAR(100) NOT NULL,
  phone      VARCHAR(20)  DEFAULT NULL,
  email      VARCHAR(100) DEFAULT NULL,
  status     ENUM('AVAILABLE','ON_DELIVERY','INACTIVE')
             NOT NULL DEFAULT 'AVAILABLE',
  PRIMARY KEY (agent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE vehicles (
  vehicle_id   VARCHAR(20) NOT NULL,
  agent_id     VARCHAR(20) NOT NULL,
  type         ENUM('VAN','MOTORBIKE','LORRY','CAR') NOT NULL,
  plate_number VARCHAR(20) NOT NULL,
  capacity_kg  DECIMAL(8,2) DEFAULT NULL,
  PRIMARY KEY (vehicle_id),
  UNIQUE KEY uq_vehicles_plate (plate_number),
  CONSTRAINT fk_vehicles_agent
    FOREIGN KEY (agent_id) REFERENCES agents (agent_id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE orders (
  order_id     VARCHAR(20) NOT NULL,
  client_id    VARCHAR(20) NOT NULL,
  order_date   DATE        NOT NULL,
  status       ENUM('PENDING','CONFIRMED','DISPATCHED',
                     'DELIVERED','CANCELLED')
               NOT NULL DEFAULT 'PENDING',
  total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  PRIMARY KEY (order_id),
  KEY idx_orders_client (client_id),
  KEY idx_orders_date (order_date),
  CONSTRAINT fk_orders_client
    FOREIGN KEY (client_id) REFERENCES clients (client_id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_items (
  item_id    INT           NOT NULL AUTO_INCREMENT,
  order_id   VARCHAR(20)   NOT NULL,
  product_id VARCHAR(20)   NOT NULL,
  quantity   INT           NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,
  line_total DECIMAL(12,2) NOT NULL,
  PRIMARY KEY (item_id),
  KEY idx_order_items_order (order_id),
  CONSTRAINT fk_order_items_order
    FOREIGN KEY (order_id) REFERENCES orders (order_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_order_items_product
    FOREIGN KEY (product_id) REFERENCES products (product_id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE deliveries (
  delivery_id    VARCHAR(20) NOT NULL,
  order_id       VARCHAR(20) NOT NULL,
  agent_id       VARCHAR(20) NOT NULL,
  scheduled_date DATE        NOT NULL,
  status         ENUM('ASSIGNED','IN_TRANSIT','DELIVERED','FAILED')
                 NOT NULL DEFAULT 'ASSIGNED',
  delivered_at   TIMESTAMP   NULL DEFAULT NULL,
  PRIMARY KEY (delivery_id),
  KEY idx_deliveries_order (order_id),
  KEY idx_deliveries_agent (agent_id),
  CONSTRAINT fk_deliveries_order
    FOREIGN KEY (order_id) REFERENCES orders (order_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_deliveries_agent
    FOREIGN KEY (agent_id) REFERENCES agents (agent_id)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notifications (
  notification_id  INT          NOT NULL AUTO_INCREMENT,
  recipient_type   ENUM('CLIENT','AGENT') NOT NULL,
  recipient_email  VARCHAR(100) NOT NULL,
  subject          VARCHAR(200) NOT NULL,
  body             TEXT         NOT NULL,
  related_order_id VARCHAR(20)  DEFAULT NULL,
  sent_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  status           ENUM('SENT','FAILED') NOT NULL DEFAULT 'SENT',
  PRIMARY KEY (notification_id),
  KEY idx_notifications_order (related_order_id),
  CONSTRAINT fk_notifications_order
    FOREIGN KEY (related_order_id) REFERENCES orders (order_id)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;