package econest;

/**
 * The kinds of vehicle EcoNest assigns to delivery agents. The constant
 * names match the values stored in the {@code vehicles.type} column.
 */
public enum VehicleType {
    /** A panel van for medium loads. */
    VAN,
    /** A motorbike for small, quick deliveries. */
    MOTORBIKE,
    /** A lorry for heavy loads. */
    LORRY,
    /** A car for light loads. */
    CAR
}
