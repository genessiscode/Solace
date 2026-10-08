package econest;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class InventoryManagerTest {

    private InventoryManager manager;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(DBConnection.isAvailable(),
                "MariaDB not reachable - skipping InventoryManager integration test");
        manager = new InventoryManager();
    }

    private void deleteQuietly(String productId) {
        try {
            manager.removeInventory(productId);
            new ProductManager().removeProduct(productId);
        } catch (RuntimeException ignored) {
            // nothing to clean up
        }
    }

    @Test
    void seededInventoryIsReadableWithProductNamesFromTheJoin() {
        Inventory item = manager.findProduct("P001");

        assertNotNull(item, "seed.sql should have loaded P001");
        assertEquals("Snake Plant premium", item.getProductName(),
                "name must come from the products table via the JOIN");
        assertEquals(15, item.getQuantity());
        assertEquals(5, item.getReorderLevel());
    }

    @Test
    void listReturnsEveryRowWithNamesPopulated() {
        ArrayList<Inventory> list = manager.getInventoryList();

        assertFalse(list.isEmpty());
        for (Inventory item : list) {
            assertNotNull(item.getProductName(),
                    "every row must have a name from the JOIN: " + item.getProductId());
            assertFalse(item.getProductName().isBlank(),
                    "name must not be blank for " + item.getProductId());
        }
    }

    @Test
    void addStockIncreasesQuantityAndRecordsTheSupplier() {
        Inventory before = manager.findProduct("P002");
        assertNotNull(before);

        manager.addStock("P002", 7, "New Supplier Co");

        Inventory after = manager.findProduct("P002");

        assertEquals(before.getQuantity() + 7, after.getQuantity());
        assertEquals("New Supplier Co", after.getSupplierName());

        // restore
        manager.removeStock("P002", 7);
    }

    @Test
    void removeStockDecreasesQuantity() {
        Inventory before = manager.findProduct("P002");
        assertNotNull(before);

        manager.removeStock("P002", 3);

        Inventory after = manager.findProduct("P002");

        assertEquals(before.getQuantity() - 3, after.getQuantity());

        // restore
        manager.addStock("P002", 3, before.getSupplierName());
    }

    @Test
    void updateChangesNameQuantityAndReorderLevel() {
        Inventory before = manager.findProduct("P002");
        assertNotNull(before);

        try {
            manager.updateInventory("P002", new Inventory(
                    "P002", "Aloe Vera Deluxe", 99, 20, before.getSupplierName()));

            Inventory after = manager.findProduct("P002");

            // The name lives in products now, so it is NOT changed here.
            assertEquals("Aloe Vera", after.getProductName());
            assertEquals(99, after.getQuantity());
            assertEquals(20, after.getReorderLevel());
        } finally {
            manager.updateInventory("P002", before);
        }
    }

    @Test
    void addInventoryCreatesBothTheStockRowAndABridgeProductRow() {
        deleteQuietly("T950");

        try {
            manager.addInventory(new Inventory(
                    "T950", "Bridge Test Product", 4, 2, "Bridge Supplier"));

            Inventory stored = manager.findProduct("T950");
            Product bridged = new ProductManager().findProduct("T950");

            assertNotNull(stored, "inventory row should exist");
            assertNotNull(bridged, "bridge product row should exist so the FK holds");
            assertEquals("Bridge Test Product", stored.getProductName());
            assertEquals(4, stored.getQuantity());
            assertEquals("Bridge Supplier", stored.getSupplierName());
        } finally {
            deleteQuietly("T950");
        }
    }

    @Test
    void removingInventoryLeavesTheProductRowAlone() {
        deleteQuietly("T951");

        try {
            manager.addInventory(new Inventory(
                    "T951", "Removal Test", 1, 1, "S"));

            manager.removeInventory("T951");

            assertNull(manager.findProduct("T951"), "stock row should be gone");
            assertNotNull(new ProductManager().findProduct("T951"),
                    "the product itself must survive its stock row");
        } finally {
            deleteQuietly("T951");
        }
    }
}
