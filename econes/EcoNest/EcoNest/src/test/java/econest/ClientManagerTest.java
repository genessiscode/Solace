package econest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ClientManagerTest {

    private ClientManager manager;

    private String testClientId;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(DBConnection.isAvailable(),
                "MariaDB not reachable - skipping ClientManager integration test");
        manager = new ClientManager();
        // client_id is VARCHAR(20), so the nano time is folded to 10 digits:
        // "TEST-C-" + at most 10 digits = at most 17 characters.
        testClientId = "TEST-C-"
                + Math.abs(System.nanoTime() % 10_000_000_000L);
    }

    /** Removes the row a test created so reruns stay green. */
    @AfterEach
    void tearDown() {
        if (manager == null || testClientId == null) {
            return; // setUp was aborted before the database was reachable
        }
        try {
            manager.removeClient(testClientId);
        } catch (RuntimeException ignored) {
            // nothing to clean up
        }
    }

    @Test
    void roundTripsAClientThroughAddFindUpdateRemove() {
        manager.addClient(new Client(testClientId, "Test Client",
                ClientType.HOUSEHOLD, "12 Green Lane", "0771234567",
                "test@example.com", ""));

        Client found = manager.findClient(testClientId);

        assertNotNull(found);
        assertEquals(testClientId, found.getClientId());
        assertEquals("Test Client", found.getName());
        assertEquals(ClientType.HOUSEHOLD, found.getClientType());
        assertEquals("12 Green Lane", found.getAddress());
        assertEquals("0771234567", found.getPhone());
        assertEquals("test@example.com", found.getEmail());
        assertEquals("", found.getCompanyName());

        manager.updateClient(new Client(testClientId, "Updated Client",
                ClientType.OFFICE, "45 Office Road", "0779876543",
                "office@example.com", "Green Offices Ltd"));

        Client updated = manager.findClient(testClientId);

        assertNotNull(updated);
        assertEquals("Updated Client", updated.getName());
        assertEquals(ClientType.OFFICE, updated.getClientType());
        assertEquals("45 Office Road", updated.getAddress());
        assertEquals("0779876543", updated.getPhone());
        assertEquals("office@example.com", updated.getEmail());
        assertEquals("Green Offices Ltd", updated.getCompanyName());

        assertTrue(manager.removeClient(testClientId));
        assertNull(manager.findClient(testClientId));
    }

    @Test
    void missingClientReturnsNull() {
        assertNull(manager.findClient("DOES-NOT-EXIST"));
    }

    @Test
    void duplicateIdIsRejected() {
        manager.addClient(new Client(testClientId, "First",
                ClientType.HOUSEHOLD, "", "", "", ""));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> manager.addClient(new Client(testClientId, "Second",
                        ClientType.OFFICE, "", "", "", "")));

        assertTrue(error.getMessage().toLowerCase()
                        .contains(testClientId.toLowerCase()),
                "message should name the duplicate id, was: "
                        + error.getMessage());
    }

    @Test
    void blankIdOrNameIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Client("   ", "Someone",
                        ClientType.HOUSEHOLD, "", "", "", ""));

        assertThrows(IllegalArgumentException.class,
                () -> new Client("C999", "  ",
                        ClientType.HOUSEHOLD, "", "", "", ""));
    }

    @Test
    void removingAnUnknownClientReportsFalse() {
        assertFalse(manager.removeClient("DOES-NOT-EXIST"));
    }

    @Test
    void listContainsTheSeededClients() {
        ArrayList<Client> clients = manager.getClientList();

        assertFalse(clients.isEmpty(), "seed.sql should have loaded clients");
        assertTrue(clients.size() >= 5,
                "expected at least the 5 seeded clients, got "
                        + clients.size());
    }
}
