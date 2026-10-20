# EcoNest - Task 4: Manage Delivery Agents

This is a standalone implementation of Task 4 from the EcoNest Java OOP project.

## Scope

Included:
- Add delivery agent
- View all delivery agents
- Update delivery agent
- Delete delivery agent
- Search by Agent ID
- Input validation
- MySQL persistence
- Java Swing frontend
- JDBC backend/service layer

Not included:
- Order management
- Assigning agents to orders
- Delivery scheduling
- Email notifications

Those are outside Task 4.

## Architecture

Frontend:
`frontend/DeliveryAgentForm.java`

        ↓

Backend:
`backend/DeliveryAgentService.java`

        ↓

Database connection:
`database/DatabaseConnection.java`

        ↓

MySQL:
`econest.delivery_agents`

## Requirements

- JDK 8 or newer
- MySQL Server
- MySQL Connector/J JDBC driver

## Setup

1. Start MySQL.
2. Run `database/econest.sql` in MySQL Workbench/phpMyAdmin.
3. Open `src/database/DatabaseConnection.java`.
4. Change USER/PASSWORD if your MySQL credentials are different.
5. Add MySQL Connector/J to the project classpath.
6. Run `Main.java`.

## Default database settings

Host: localhost
Port: 3306
Database: econest
User: root
Password: empty

Change these in `DatabaseConnection.java` if needed.

## Important

If your MySQL password is not empty, change:

`private static final String PASSWORD = "";`

to your actual local MySQL password.

## OOP demonstrated

- Encapsulation
- Classes and objects
- Constructor
- Getters/setters
- Service layer
- Separation of concerns
- JDBC database operations
- Array/List handling

## Testing checklist

1. Add a new agent.
2. Try adding the same Agent ID again.
3. Select an agent from the table.
4. Update the agent.
5. Search by Agent ID.
6. Delete an agent.
7. Try submitting empty fields.
8. Try an invalid phone number.
9. Restart the application and confirm database records remain.
