package econest;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomerDAOTest {

    private final CustomerDAO dao = new CustomerDAO();

    @Test
    void customerStoresConstructorValues() {
        Customer customer = new Customer(
                "C001", "Nimal Perera", "HOUSEHOLD",
                "12 Temple Road, Colombo", "0771234567",
                "nimal@example.com", null);

        assertEquals("C001", customer.getClientId());
        assertEquals("Nimal Perera", customer.getName());
        assertEquals("HOUSEHOLD", customer.getClientType());
        assertEquals("12 Temple Road, Colombo", customer.getAddress());
        assertEquals("0771234567", customer.getPhone());
        assertEquals("nimal@example.com", customer.getEmail());
        assertNull(customer.getCompanyName());
    }

    @Test
    void customerValidationRejectsInvalidData() {
        assertThrows(IllegalArgumentException.class,
                () -> new Customer(null, " ", "HOUSEHOLD", null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Customer(null, "Test", "COMPANY", null, null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Customer(null, "Test", "HOUSEHOLD", null, null, "not-an-email", null));
        assertThrows(IllegalArgumentException.class,
                () -> new Customer(null, "Test", "HOUSEHOLD", null, "abc", null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Customer(null, "Test", "OFFICE", null, null, null, " "));

        Customer household = new Customer(
                null, "  Nimal Perera  ", "HOUSEHOLD", null, null, null, "Ignored Company");
        assertEquals("Nimal Perera", household.getName());
        assertNull(household.getCompanyName());
    }

    @Test
    void findByIdReturnsCustomerOrNull() throws Exception {
        assumeDatabase();

        String token = uniqueToken();
        String id = null;
        try {
            id = dao.create(new Customer(
                    null, "Lookup Customer " + token, "HOUSEHOLD",
                    null, null, null, null));

            Customer found = dao.findById(id);
            assertNotNull(found);
            assertEquals(id, found.getClientId());
            assertEquals("Lookup Customer " + token, found.getName());
            assertNull(dao.findById("C999999999"));
        } finally {
            deleteQuietly(id);
        }
    }

    @Test
    void createSearchUpdateAndDeleteCustomer() throws Exception {
        assumeDatabase();

        String token = uniqueToken();
        String name = "Test Customer " + token;
        String id = null;

        try {
            id = dao.create(new Customer(
                    null, name, "OFFICE", "1 Test Road",
                    "0770000000", "customer-" + token + "@example.com",
                    "Test Company"));

            assertNotNull(id);
            assertTrue(id.matches("C\\d+"), "generated ID should look like C001");

            List<Customer> created = dao.getAll(name, "ALL");
            assertEquals(1, created.size());
            Customer customer = created.get(0);
            assertEquals(id, customer.getClientId());
            assertEquals("OFFICE", customer.getClientType());
            assertEquals("Test Company", customer.getCompanyName());

            dao.update(new Customer(
                    id, name + " Updated", "HOUSEHOLD",
                    null, null, null, null));

            List<Customer> updated = dao.getAll(name + " Updated", "ALL");
            assertEquals(1, updated.size());
            Customer changed = updated.get(0);
            assertEquals("HOUSEHOLD", changed.getClientType());
            assertNull(changed.getPhone());
            assertNull(changed.getEmail());
            assertNull(changed.getCompanyName());

            dao.delete(id);
            id = null;

            assertTrue(dao.getAll(name + " Updated", "ALL").isEmpty(),
                    "deleted customer should no longer be returned");
        } finally {
            if (id != null) {
                try {
                    dao.delete(id);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    @Test
    void filtersCustomersByTypeAndSearchTerm() throws Exception {
        assumeDatabase();

        String token = uniqueToken();
        String householdName = "Household " + token;
        String officeName = "Office " + token;
        String householdId = null;
        String officeId = null;

        try {
            householdId = dao.create(new Customer(
                    null, householdName, "HOUSEHOLD",
                    null, null, null, null));
            officeId = dao.create(new Customer(
                    null, officeName, "OFFICE",
                    null, null, null, "Filter Test Ltd"));

            List<Customer> households = dao.getAll(token, "HOUSEHOLD");
            assertEquals(1, households.size());
            assertEquals(householdId, households.get(0).getClientId());

            List<Customer> offices = dao.getAll(token, "OFFICE");
            assertEquals(1, offices.size());
            assertEquals(officeId, offices.get(0).getClientId());

            List<Customer> allMatches = dao.getAll(token, "ALL");
            assertEquals(2, allMatches.size());
        } finally {
            deleteQuietly(householdId);
            deleteQuietly(officeId);
        }
    }

    @Test
    void updateAndDeleteRejectUnknownCustomer() throws Exception {
        assumeDatabase();

        Customer missing = new Customer(
                "C999999999", "Missing Customer", "HOUSEHOLD",
                null, null, null, null);

        SQLException updateError = assertThrows(SQLException.class,
                () -> dao.update(missing));
        assertTrue(updateError.getMessage().contains("not found"));

        SQLException deleteError = assertThrows(SQLException.class,
                () -> dao.delete("C999999999"));
        assertTrue(deleteError.getMessage().contains("not found"));
    }

    private void assumeDatabase() {
        Assumptions.assumeTrue(DBConnection.isAvailable(),
                "MariaDB not reachable - skipping CustomerDAO integration test");
    }

    private String uniqueToken() {
        return Long.toUnsignedString(System.nanoTime());
    }

    private void deleteQuietly(String clientId) {
        if (clientId == null) {
            return;
        }
        try {
            dao.delete(clientId);
        } catch (SQLException ignored) {
        }
    }
}
