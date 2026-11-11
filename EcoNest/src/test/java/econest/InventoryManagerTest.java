package econest;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class InventoryManagerTest {

    private InventoryManager inventoryManager;
    private ProductManager productManager;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(DBConnection.isAvailable(),
                "MariaDB not reachable - skipping InventoryManager integration test");
        inventoryManager = new InventoryManager();
        productManager = new ProductManager();
    }

    private String uniqueId(String prefix) {
        return prefix + (System.nanoTime() % 1_000_000_000L);
    }

    private void createProduct(String productId) {
        productManager.addProduct(new Plant(
                productId, "Stock Test Plant", 100.0, 3,
                "Created by InventoryManagerTest", "HIGH", true));
    }

    private void deleteQuietly(String productId) {
        try {
            inventoryManager.removeInventory(productId);
        } catch (RuntimeException ignored) {
            // nothing to clean up
        }
        try {
            productManager.removeProduct(productId);
        } catch (RuntimeException ignored) {
            // nothing to clean up
        }
    }

    @Test
    void roundTripsAStockRecordWithItsSupplier() {
        String productId = uniqueId("T3-");

        try {
            createProduct(productId);

            Inventory item = new Inventory(
                    productId, "Stock Test Plant", 12, 4,
                    "GreenLeaf Supplies");

            inventoryManager.addInventory(item);

            Inventory found = inventoryManager.findProduct(productId);

            assertNotNull(found);
            assertEquals("Stock Test Plant", found.getProductName());
            assertEquals(12, found.getQuantity());
            assertEquals(4, found.getReorderLevel());
            assertEquals("GreenLeaf Supplies", found.getSupplierName());

            boolean listed = false;
            for (Inventory row : inventoryManager.getInventoryList()) {
                if (productId.equals(row.getProductId())) {
                    listed = true;
                    assertEquals("GreenLeaf Supplies", row.getSupplierName());
                }
            }
            assertTrue(listed, "the inventory list should contain the test row");

            inventoryManager.updateInventory(productId,
                    new Inventory(productId, "Stock Test Plant",
                            20, 6, "Ceylon Nursery"));

            Inventory updated = inventoryManager.findProduct(productId);

            assertEquals(20, updated.getQuantity());
            assertEquals(6, updated.getReorderLevel());
            assertEquals("Ceylon Nursery", updated.getSupplierName());

            assertTrue(inventoryManager.removeInventory(productId));
            assertNull(inventoryManager.findProduct(productId));
            assertNotNull(productManager.findProduct(productId),
                    "removing stock must not delete the catalogue product");

        } finally {
            deleteQuietly(productId);
        }
    }

    @Test
    void lowStockListContainsOnlyItemsAtOrBelowTheirReorderLevel() {
        String lowId = uniqueId("T3L-");
        String okId = uniqueId("T3O-");

        try {
            createProduct(lowId);
            createProduct(okId);

            inventoryManager.addInventory(new Inventory(
                    lowId, "Stock Test Plant", 2, 5, "Low Supplier"));
            inventoryManager.addInventory(new Inventory(
                    okId, "Stock Test Plant", 10, 5, "Ok Supplier"));

            ArrayList<Inventory> low = inventoryManager.getLowStockItems();

            boolean foundLow = false;
            boolean foundOk = false;

            for (Inventory item : low) {
                if (lowId.equals(item.getProductId())) {
                    foundLow = true;
                }
                if (okId.equals(item.getProductId())) {
                    foundOk = true;
                }
            }

            assertTrue(foundLow, "the low-stock product should be listed");
            assertFalse(foundOk, "the healthy product should not be listed");

        } finally {
            deleteQuietly(lowId);
            deleteQuietly(okId);
        }
    }

    @Test
    void stockMovementsUpdateQuantityAndSupplier() {
        String productId = uniqueId("T3M-");

        try {
            createProduct(productId);
            inventoryManager.addInventory(new Inventory(
                    productId, "Stock Test Plant", 5, 2, "First Supplier"));

            inventoryManager.addStock(productId, 7, "Second Supplier");

            Inventory afterIn = inventoryManager.findProduct(productId);

            assertEquals(12, afterIn.getQuantity());
            assertEquals("Second Supplier", afterIn.getSupplierName());

            inventoryManager.removeStock(productId, 10);

            Inventory afterOut = inventoryManager.findProduct(productId);

            assertEquals(2, afterOut.getQuantity());

            assertThrows(IllegalArgumentException.class,
                    () -> inventoryManager.removeStock(productId, 3));

            assertEquals(2, inventoryManager.findProduct(productId)
                    .getQuantity(), "a failed stock-out must not change stock");

        } finally {
            deleteQuietly(productId);
        }
    }

    @Test
    void duplicateInventoryForAProductIsRejected() {
        String productId = uniqueId("T3D-");

        try {
            createProduct(productId);
            inventoryManager.addInventory(new Inventory(
                    productId, "Stock Test Plant", 1, 1, "Supplier"));

            IllegalStateException error = assertThrows(
                    IllegalStateException.class,
                    () -> inventoryManager.addInventory(new Inventory(
                            productId, "Stock Test Plant", 2, 1, "Supplier")));

            assertTrue(error.getMessage().contains(productId),
                    "message should name the duplicate product, was: "
                            + error.getMessage());

        } finally {
            deleteQuietly(productId);
        }
    }

    @Test
    void missingRowsReturnNullOrFalse() {
        assertNull(inventoryManager.findProduct("DOES-NOT-EXIST"));
        assertFalse(inventoryManager.removeInventory("DOES-NOT-EXIST"));
    }

    @Test
    void productChoicesIncludeCatalogueProducts() {
        String productId = uniqueId("T3C-");

        try {
            createProduct(productId);

            boolean found = false;

            for (InventoryManager.ProductChoice choice
                    : inventoryManager.getProductChoices()) {
                if (productId.equals(choice.getProductId())) {
                    found = true;
                    assertEquals("Stock Test Plant", choice.getProductName());
                }
            }

            assertTrue(found, "catalogue product should appear in the picker");

        } finally {
            deleteQuietly(productId);
        }
    }

    @Test
    void invalidStockInIsRejected() {
        String productId = uniqueId("T3I-");

        try {
            createProduct(productId);
            inventoryManager.addInventory(new Inventory(
                    productId, "Stock Test Plant", 1, 1, "Supplier"));

            assertThrows(IllegalArgumentException.class,
                    () -> inventoryManager.addStock(productId, 0, "Supplier"));
            assertThrows(IllegalArgumentException.class,
                    () -> inventoryManager.addStock(productId, 2, " "));
            assertThrows(IllegalArgumentException.class,
                    () -> inventoryManager.addStock(
                            "DOES-NOT-EXIST", 2, "Supplier"));

        } finally {
            deleteQuietly(productId);
        }
    }
}
