# EcoNest - Home & Garden Management

Java Swing desktop application for managing plants, planters, gardening kits, clients, inventory, orders, and deliveries.

## Project Structure

```
EcoNest/
├── pom.xml                                    # Maven build configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── econest/                       # Main package (flat structure)
│   │   │       ├── Main.java                  # Application entry point
│   │   │       ├── MainFrame.java             # Dashboard with 9 module cards
│   │   │       ├── Product.java               # Abstract base class
│   │   │       ├── ProductCategory.java       # Enum: PLANT, PLANTER, KIT
│   │   │       ├── ProductManager.java        # CRUD for product catalogue
│   │   │       ├── Inventory.java             # Stock item model
│   │   │       ├── InventoryManager.java      # Stock management with JOIN
│   │   │       ├── InventoryFrame.java        # Inventory UI (6 columns)
│   │   │       ├── Client.java                # Client model (HOUSEHOLD/OFFICE)
│   │   │       ├── ClientType.java            # Enum
│   │   │       ├── ClientManager.java         # Client CRUD
│   │   │       ├── ClientFrame.java           # Client UI
│   │   │       ├── Agent.java                 # Delivery agent model
│   │   │       ├── AgentStatus.java           # Enum: AVAILABLE, ON_DELIVERY, INACTIVE
│   │   │       ├── AgentManager.java          # Agent CRUD
│   │   │       ├── AgentFrame.java            # Agent UI
│   │   │       ├── Vehicle.java               # Vehicle model
│   │   │       ├── VehicleType.java           # Enum: VAN, MOTORBIKE, LORRY, CAR
│   │   │       ├── Order.java                 # Order model
│   │   │       ├── OrderItem.java             # Order line item
│   │   │       ├── OrderStatus.java           # Enum: PENDING..CANCELLED
│   │   │       ├── OrderManager.java          # Order CRUD
│   │   │       ├── OrderFrame.java            # Order UI
│   │   │       ├── Delivery.java              # Delivery assignment model
│   │   │       ├── DeliveryManager.java       # Delivery assignment DB ops
│   │   │       ├── DeliveryFrame.java         # Delivery assignment UI
│   │   │       ├── DeliveryDBConnection.java  # Module JDBC connection helper
│   │   │       ├── DeliveryAssignmentApp.java # Standalone launcher (Task 6)
│   │   │       ├── ReportManager.java         # Monthly reports
│   │   │       ├── ReportFrame.java           # Reports UI
│   │   │       ├── DBConnection.java          # JDBC connection pool
│   │   │       └── AppConfig.java             # Config from classpath
│   │   └── resources/
│   │       ├── config.properties.template     # DB config template (committed)
│   │       └── db/
│   │           ├── schema.sql                 # Full 9-table schema (InnoDB)
│   │           └── seed.sql                   # Demo data
│   └── test/
│       └── java/
│           └── econest/                       # JUnit 5 tests
│               ├── ProductTest.java
│               └── ProductManagerTest.java
│               ├── InventoryTest.java
│               ├── InventoryManagerTest.java
│               ├── DBConnectionTest.java
│               └── ...
```

## 9 Business Features (Modules)

| # | Module | Status | Assigned To |
|---|--------|--------|-------------|
| 1 | Product Catalogue | **Built** | Sanuk Jayathunga |
| 2 | Client Management | Planned | Eranda Sandeep |
| 3 | Stock / Inventory | Planned | K.A.R.P Sathsarani |
| 4 | Delivery Agents | Planned | V Hemakesh |
| 5 | Client Orders | Planned | Kaveesha Nawod |
| 6 | Delivery Assignment | **Built** | Umar Luqman |
| 7 | Monthly Reports | Planned | Sanuk Jayathunga |
| 8 | Client Notifications | Planned | Kasun Chiwantha |
| 9 | Agent Notifications | Planned | Kasun Chiwantha |

## Tech Stack

- **Java 21** (maven.compiler.release=21)
- **Maven** 3.9+
- **Java Swing** for GUI
- **MariaDB/MySQL** 8+ with InnoDB
- **mysql-connector-j** 8.0.33
- **JUnit 5.10.1** for testing



## Team Members

| Name | Student ID | Role | Tasks |
|------|------------|------|-------|
| Sanuk Jayathunga | SA25610774 | Leader | 1, 7 |
| Eranda Sandeep | SA25610771 | Member | 2 |
| K.A.R.P Sathsarani | SA25610882 | Member | 3 |
| V Hemakesh | SA25610757 | Member | 4 |
| Kaveesha Nawod | SA25610776 | Member | 5 |
| Umar Luqman | SA25610830 | Member | 6 |
| Kasun Chiwantha | SA25610776 | Member | 8, 9 |

## Development Guidelines

- Package: `econest` (flat, no sub-packages)
- All tables use `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4`
- Config file `config.properties` is **gitignored** - use the template
- Model validation: `IllegalArgumentException` for invalid input
- Tests use `Assumptions.assumeTrue(DBConnection.isAvailable())` to skip when DB is down
- Commit frequently with descriptive messages

## License

Educational project - Solace Group OOP Project 2027
