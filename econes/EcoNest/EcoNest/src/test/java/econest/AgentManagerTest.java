package econest;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class AgentManagerTest {

    /**
     * The date passed to {@link AgentManager#getAgentTableList(String)}.
     *
     * <p>No seed delivery is scheduled this far out, so every agent is
     * listed and these assertions stay deterministic. Tests that need the
     * scheduling filter to hide an agent create a delivery on their own
     * date instead.</p>
     */
    private static final String FAR_FUTURE_DATE = "2099-12-31";

    private AgentManager manager;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(DBConnection.isAvailable(),
                "MariaDB not reachable - skipping AgentManager integration test");
        manager = new AgentManager();
    }

    /** Removes a test agent if a previous run left one behind. */
    private void deleteQuietly(String agentId) {
        try {
            manager.removeAgent(agentId);
        } catch (RuntimeException ignored) {
            // nothing to clean up
        }
    }

    /** A unique id short enough for the VARCHAR(20) columns. */
    private String uniqueId(String prefix) {
        return prefix + (System.nanoTime() % 10_000_000_000L);
    }

    @Test
    void roundTripsAnAgentAndItsVehicle() {
        String agentId = uniqueId("TEST-A-");
        String vehicleId = uniqueId("TV-");
        String plateNumber = uniqueId("TST-");

        try {
            manager.addAgent(new Agent(agentId, "Test Agent",
                    "0700000000", "test@econest.example",
                    AgentStatus.AVAILABLE));
            manager.addVehicle(new Vehicle(vehicleId, agentId,
                    VehicleType.MOTORBIKE, plateNumber, 55.5));

            Agent found = manager.findAgent(agentId);

            assertNotNull(found);
            assertEquals("Test Agent", found.getName());
            assertEquals("0700000000", found.getPhone());
            assertEquals(AgentStatus.AVAILABLE, found.getStatus());

            Vehicle vehicle = manager.findVehicleByAgent(agentId);

            assertNotNull(vehicle);
            assertEquals(vehicleId, vehicle.getVehicleId());
            assertEquals(VehicleType.MOTORBIKE, vehicle.getType());
            assertEquals(plateNumber, vehicle.getPlateNumber());
            assertEquals(55.5, vehicle.getCapacityKg(), 0.001);

            manager.updateAgent(new Agent(agentId, "Test Agent Updated",
                    "0711111111", "updated@econest.example",
                    AgentStatus.ON_DELIVERY));

            Agent updated = manager.findAgent(agentId);

            assertEquals("Test Agent Updated", updated.getName());
            assertEquals(AgentStatus.ON_DELIVERY, updated.getStatus());

            boolean rowFound = false;

            for (AgentManager.AgentRow row : manager.getAgentTableList(FAR_FUTURE_DATE)) {
                if (agentId.equals(row.getAgentId())) {
                    rowFound = true;
                    assertEquals("Test Agent Updated", row.getName());
                    assertEquals(vehicleId, row.getVehicleId());
                    assertEquals(VehicleType.MOTORBIKE, row.getVehicleType());
                    assertEquals(plateNumber, row.getPlateNumber());
                    assertEquals(55.5, row.getCapacityKg(), 0.001);
                }
            }

            assertTrue(rowFound, "the joined table should list the test agent");

            assertTrue(manager.removeVehicle(vehicleId));
            assertNull(manager.findVehicleByAgent(agentId));
        } finally {
            deleteQuietly(agentId);
        }
    }

    @Test
    void anAgentWithoutAVehicleHasNullVehicleColumns() {
        String agentId = uniqueId("TEST-A-");

        try {
            manager.addAgent(new Agent(agentId, "Vehicleless Agent",
                    "", "", AgentStatus.INACTIVE));

            boolean rowFound = false;

            for (AgentManager.AgentRow row : manager.getAgentTableList(FAR_FUTURE_DATE)) {
                if (agentId.equals(row.getAgentId())) {
                    rowFound = true;
                    assertNull(row.getVehicleId());
                    assertNull(row.getVehicleType());
                    assertNull(row.getPlateNumber());
                    assertNull(row.getCapacityKg());
                }
            }

            assertTrue(rowFound, "the joined table should list the test agent");
        } finally {
            deleteQuietly(agentId);
        }
    }

    @Test
    void aMissingCapacityRoundTripsAsNullInTheJoinedTable() {
        String agentId = uniqueId("TEST-A-");
        String vehicleId = uniqueId("TV-");
        String plateNumber = uniqueId("TST-");

        try {
            manager.addAgent(new Agent(agentId, "No Capacity Agent",
                    "", "", AgentStatus.AVAILABLE));
            manager.addVehicle(new Vehicle(vehicleId, agentId,
                    VehicleType.CAR, plateNumber, Double.NaN));

            Vehicle vehicle = manager.findVehicleByAgent(agentId);

            assertNotNull(vehicle);
            assertTrue(Double.isNaN(vehicle.getCapacityKg()),
                    "a NULL capacity should read back as NaN");

            for (AgentManager.AgentRow row : manager.getAgentTableList(FAR_FUTURE_DATE)) {
                if (agentId.equals(row.getAgentId())) {
                    assertEquals(vehicleId, row.getVehicleId());
                    assertNull(row.getCapacityKg(),
                            "a NULL capacity should show as a null cell");
                }
            }
        } finally {
            deleteQuietly(agentId);
        }
    }

    @Test
    void missingAgentAndVehicleReturnNull() {
        assertNull(manager.findAgent("DOES-NOT-EXIST"));
        assertNull(manager.findVehicleByAgent("DOES-NOT-EXIST"));
    }

    @Test
    void duplicateAgentIdIsRejected() {
        String agentId = uniqueId("TEST-A-");

        try {
            manager.addAgent(new Agent(agentId, "First",
                    "", "", AgentStatus.AVAILABLE));

            IllegalStateException error = assertThrows(
                    IllegalStateException.class,
                    () -> manager.addAgent(new Agent(agentId, "Second",
                            "", "", AgentStatus.AVAILABLE)));

            assertTrue(error.getMessage().toLowerCase().contains(
                            agentId.toLowerCase()),
                    "message should name the duplicate id, was: "
                            + error.getMessage());
        } finally {
            deleteQuietly(agentId);
        }
    }

    @Test
    void duplicatePlateIsRejectedWithAReadableMessage() {
        String agentId = uniqueId("TEST-A-");

        try {
            manager.addAgent(new Agent(agentId, "Plate Checker",
                    "", "", AgentStatus.AVAILABLE));

            // CAB-1234 is the seeded plate of V001 - it must stay unique.
            IllegalStateException error = assertThrows(
                    IllegalStateException.class,
                    () -> manager.addVehicle(new Vehicle(
                            uniqueId("TV-"), agentId,
                            VehicleType.VAN, "CAB-1234", 100.0)));

            assertTrue(error.getMessage().toLowerCase().contains("cab-1234"),
                    "message should name the plate, was: "
                            + error.getMessage());
        } finally {
            deleteQuietly(agentId);
        }
    }

    @Test
    void removingAnAgentWithDeliveryHistoryIsRefused() {
        // A001 has delivery D001, so the RESTRICT foreign key blocks deletion.
        boolean removed = manager.removeAgent("A001");

        assertFalse(removed, "A001 has deliveries and must not be deleted");
        assertNotNull(manager.findAgent("A001"), "A001 must still exist");
    }

    @Test
    void removingAnAbsentAgentReportsFalse() {
        assertFalse(manager.removeAgent("DOES-NOT-EXIST"));
        assertFalse(manager.removeVehicle("DOES-NOT-EXIST"));
    }

    @Test
    void listContainsTheSeededAgents() {
        ArrayList<Agent> agents = manager.getAgentList();

        assertFalse(agents.isEmpty(), "schema.sql should have loaded agents");
        assertTrue(agents.size() >= 4,
                "expected at least the 4 seeded agents, got "
                        + agents.size());
        assertEquals("A001", agents.get(0).getAgentId(),
                "agents should be ordered by agent_id");

        ArrayList<AgentManager.AgentRow> rows = manager.getAgentTableList(FAR_FUTURE_DATE);

        assertTrue(rows.size() >= 4,
                "the joined table should list every seeded agent");

        boolean allSeededVehiclesJoined = true;

        for (AgentManager.AgentRow row : rows) {
            if (row.getAgentId().startsWith("A00")
                    && row.getVehicleId() == null) {
                allSeededVehiclesJoined = false;
            }
        }

        assertTrue(allSeededVehiclesJoined,
                "every seeded agent has a vehicle, so the join should show it");
    }
}
