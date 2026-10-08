package econest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    @Test
    void addStockIncreasesQuantity() {
        Inventory item = new Inventory("P001", "Snake Plant", 10, 5, "Supplier");

        item.addStock(20);

        assertEquals(30, item.getQuantity());
    }

    @Test
    void addStockIgnoresNonPositiveAmounts() {
        Inventory item = new Inventory("P001", "Snake Plant", 10, 5, "Supplier");

        item.addStock(0);
        item.addStock(-5);

        assertEquals(10, item.getQuantity());
    }

    @Test
    void removeStockSucceedsWithinAvailableQuantity() {
        Inventory item = new Inventory("P001", "Snake Plant", 10, 5, "Supplier");

        assertTrue(item.removeStock(10));
        assertEquals(0, item.getQuantity());
    }

    @Test
    void removeStockRefusesMoreThanAvailable() {
        Inventory item = new Inventory("P001", "Snake Plant", 10, 5, "Supplier");

        assertFalse(item.removeStock(100));
        assertEquals(10, item.getQuantity(), "a refused removal must not change stock");
    }

    @Test
    void removeStockRefusesNonPositiveAmounts() {
        Inventory item = new Inventory("P001", "Snake Plant", 10, 5, "Supplier");

        assertFalse(item.removeStock(0));
        assertFalse(item.removeStock(-1));
        assertEquals(10, item.getQuantity());
    }

    @Test
    void lowStockIsTrueAtOrBelowTheReorderLevel() {
        Inventory item = new Inventory("P001", "Snake Plant", 5, 5, "Supplier");

        assertTrue(item.isLowStock(), "quantity equal to the reorder level is low");

        item.removeStock(1);
        assertTrue(item.isLowStock());
    }

    @Test
    void lowStockIsFalseAboveTheReorderLevel() {
        Inventory item = new Inventory("P001", "Snake Plant", 6, 5, "Supplier");

        assertFalse(item.isLowStock());
    }

    @Test
    void supplierNameIsRetained() {
        Inventory item = new Inventory("P001", "Snake Plant", 10, 5, "GreenLeaf Supplies");

        assertEquals("GreenLeaf Supplies", item.getSupplierName());
    }
}
