package econest;

/**
 * The stages an order moves through. The constant names match the values
 * stored in the {@code orders.status} column.
 */
public enum OrderStatus {
    /** Placed but not yet confirmed by EcoNest. */
    PENDING,
    /** Confirmed and waiting to be packed. */
    CONFIRMED,
    /** On its way to the client. */
    DISPATCHED,
    /** Handed over to the client. */
    DELIVERED,
    /** Called off before delivery. */
    CANCELLED
}
