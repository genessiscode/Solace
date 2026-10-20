CREATE DATABASE IF NOT EXISTS econest;
USE econest;

CREATE TABLE IF NOT EXISTS delivery_agents (
    agent_id VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(10) NOT NULL,
    address VARCHAR(255) NOT NULL,
    vehicle_number VARCHAR(30) NOT NULL,
    vehicle_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL
);

INSERT INTO delivery_agents
(agent_id, name, phone, address, vehicle_number, vehicle_type, status)
VALUES
('DA001', 'Kasun Perera', '0771234567', 'Colombo', 'WP CAB-1234', 'Van', 'Available'),
('DA002', 'Nimal Silva', '0712345678', 'Kotte', 'WP CBA-5678', 'Motorcycle', 'Available');
