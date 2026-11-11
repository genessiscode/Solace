package econest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    @Test
    void storesTheStockRecordAndNormalisesTheSupplier() {

        Inventory item = new Inventory(
                "P001", "Snake Plant", 10, 5, "  GreenLeaf Supplies  ");

        assertEquals("P001", item.getProductId());
        assertEquals("Snake Plant", item.getProductName());
        assertEquals(10, item.getQuantity());
        assertEquals(5, item.getReorderLevel());
        assertEquals("GreenLeaf Supplies", item.getSupplierName());
        assertFalse(item.isLowStock());
        assertEquals(0, item.getStockGap());
    }

    @Test
    void aMissingSupplierBecomesAnEmptyString() {

        Inventory item = new Inventory("P001", "Snake Plant", 10, 5, null);

        assertEquals("", item.getSupplierName());
    }

    @Test
    void stockAtOrBelowTheReorderLevelIsLow() {

        Inventory atLevel = new Inventory("P001", "Snake Plant", 5, 5);
        Inventory below = new Inventory("P002", "Aloe Vera", 3, 5);

        assertTrue(atLevel.isLowStock());
        assertTrue(below.isLowStock());
        assertEquals(2, below.getStockGap());
    }

    @Test
    void addAndRemoveStockChangeTheQuantity() {

        Inventory item = new Inventory("P001", "Snake Plant", 10, 5);

        item.addStock(4);
        assertEquals(14, item.getQuantity());

        assertTrue(item.removeStock(9));
        assertEquals(5, item.getQuantity());

        assertFalse(item.removeStock(6));
        assertEquals(5, item.getQuantity());
    }

    @Test
    void invalidStockMovementsAreRejected() {

        Inventory item = new Inventory("P001", "Snake Plant", 10, 5);

        assertThrows(IllegalArgumentException.class, () -> item.addStock(0));
        assertThrows(IllegalArgumentException.class, () -> item.addStock(-2));
        assertFalse(item.removeStock(0));
        assertFalse(item.removeStock(-1));
        assertEquals(10, item.getQuantity());
    }

    @Test
    void invalidFieldsAreRejected() {

        assertThrows(IllegalArgumentException.class,
                () -> new Inventory(" ", "Snake Plant", 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Inventory("P001", " ", 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Inventory("P001", "Snake Plant", -1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Inventory("P001", "Snake Plant", 1, -1));
    }
}
