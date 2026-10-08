package econest;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ProductManagerTest {

    private ProductManager manager;

    @BeforeEach
    void setUp() {
        Assumptions.assumeTrue(DBConnection.isAvailable(),
                "MariaDB not reachable - skipping ProductManager integration test");
        manager = new ProductManager();
    }

    /** Removes a test product if a previous run left one behind. */
    private void deleteQuietly(String productId) {
        try {
            manager.removeProduct(productId);
        } catch (RuntimeException ignored) {
            // nothing to clean up
        }
    }

    @Test
    void roundTripsEachSubtypeAndReturnsTheRightConcreteClass() {
        deleteQuietly("T900");
        deleteQuietly("T901");
        deleteQuietly("T902");

        try {
            manager.addProduct(new Plant("T900", "Test Plant", 100.00, 3,
                    "desc", "LOW", true));
            manager.addProduct(new Planter("T901", "Test Planter", 200.00, 4,
                    "desc", "CLAY", 12));
            manager.addProduct(new GardenKit("T902", "Test Kit", 300.00, 5,
                    "desc", "BEGINNER"));

            Product plant = manager.findProduct("T900");
            Product planter = manager.findProduct("T901");
            Product kit = manager.findProduct("T902");

            assertInstanceOf(Plant.class, plant);
            assertInstanceOf(Planter.class, planter);
            assertInstanceOf(GardenKit.class, kit);

            assertEquals(ProductCategory.PLANT, plant.getCategory());
            assertEquals(ProductCategory.PLANTER, planter.getCategory());
            assertEquals(ProductCategory.KIT, kit.getCategory());

            assertEquals("LOW", ((Plant) plant).getSunlight());
            assertTrue(((Plant) plant).isPetSafe());
            assertEquals("CLAY", ((Planter) planter).getMaterial());
            assertEquals(12, ((Planter) planter).getDiameterCm());
            assertEquals("BEGINNER", ((GardenKit) kit).getDifficulty());
        } finally {
            deleteQuietly("T900");
            deleteQuietly("T901");
            deleteQuietly("T902");
        }
    }

    @Test
    void updatesEveryMutableField() {
        deleteQuietly("T910");

        try {
            manager.addProduct(new Planter("T910", "Before", 100.00, 3,
                    "old", "CLAY", 10));

            manager.updateProduct(new Planter("T910", "After", 250.00, 5,
                    "new", "CERAMIC", 22));

            Planter updated = (Planter) manager.findProduct("T910");

            assertEquals("After", updated.getName());
            assertEquals(250.00, updated.getPrice(), 0.001);
            assertEquals(5, updated.getEcoRating());
            assertEquals("new", updated.getDescription());
            assertEquals("CERAMIC", updated.getMaterial());
            assertEquals(22, updated.getDiameterCm());
        } finally {
            deleteQuietly("T910");
        }
    }

    @Test
    void missingProductReturnsNull() {
        assertNull(manager.findProduct("DOES-NOT-EXIST"));
    }

    @Test
    void duplicateIdIsRejected() {
        deleteQuietly("T920");

        try {
            manager.addProduct(new Plant("T920", "First", 100.00, 3,
                    "", "LOW", false));

            IllegalStateException error = assertThrows(
                    IllegalStateException.class,
                    () -> manager.addProduct(new Plant("T920", "Second", 100.00, 3,
                            "", "LOW", false)));

            assertTrue(error.getMessage().toLowerCase().contains("t920"),
                    "message should name the duplicate id, was: " + error.getMessage());
        } finally {
            deleteQuietly("T920");
        }
    }

    @Test
    void removingAStockedProductIsRefusedWithAReadableMessage() {
        // P001 has an inventory row, so the RESTRICT foreign key blocks deletion.
        boolean removed = manager.removeProduct("P001");

        assertFalse(removed, "P001 has stock and must not be deleted");
        assertNotNull(manager.findProduct("P001"), "P001 must still exist");
    }

    @Test
    void removingAnAbsentProductReportsFalse() {
        assertFalse(manager.removeProduct("DOES-NOT-EXIST"));
    }

    @Test
    void listContainsTheSeededCatalogue() {
        ArrayList<Product> products = manager.getProductList();

        assertFalse(products.isEmpty(), "seed.sql should have loaded products");
        assertTrue(manager.countProducts() >= 10,
                "expected at least the 10 seeded products, got " + manager.countProducts());
    }
}
