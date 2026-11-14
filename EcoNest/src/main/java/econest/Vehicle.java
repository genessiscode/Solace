package econest;

/**
 * The vehicle a delivery agent uses. One vehicle belongs to exactly one
 * agent, and the plate number is unique across the whole fleet.
 *
 * <p>Holds the attributes every vehicle has, and validates them on
 * construction so an invalid vehicle can never reach the database. Callers
 * catch {@link IllegalArgumentException} and report it to the user.</p>
 *
 * <p>The capacity is a plain {@code double} because the model is always
 * present; {@code NaN} means "no capacity recorded" and is stored as SQL
 * {@code NULL} by {@link AgentManager}.</p>
 */
public class Vehicle {

    private final String vehicleId;

    private final String agentId;

    private final VehicleType type;

    private final String plateNumber;

    private final double capacityKg;

    /**
     * @param vehicleId   the vehicle identifier, for example {@code V001}
     * @param agentId     the agent the vehicle belongs to
     * @param type        the kind of vehicle
     * @param plateNumber the licence plate; unique across the fleet
     * @param capacityKg  the load capacity in kilograms, or {@code NaN}
     *                    when no capacity is recorded
     * @throws IllegalArgumentException if {@code vehicleId},
     *                                  {@code agentId} or
     *                                  {@code plateNumber} is blank, or
     *                                  {@code type} is {@code null}
     */
    public Vehicle(
            String vehicleId,
            String agentId,
            VehicleType type,
            String plateNumber,
            double capacityKg) {

        if (vehicleId == null || vehicleId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Vehicle ID cannot be empty.");
        }

        if (agentId == null || agentId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Agent ID cannot be empty.");
        }

        if (type == null) {
            throw new IllegalArgumentException(
                    "Vehicle type cannot be empty.");
        }

        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Plate number cannot be empty.");
        }

        this.vehicleId = vehicleId.trim();
        this.agentId = agentId.trim();
        this.type = type;
        this.plateNumber = plateNumber.trim();
        this.capacityKg = capacityKg;
    }

    /**
     * @return the vehicle identifier
     */
    public String getVehicleId() {
        return vehicleId;
    }

    /**
     * @return the agent the vehicle belongs to
     */
    public String getAgentId() {
        return agentId;
    }

    /**
     * @return the kind of vehicle
     */
    public VehicleType getType() {
        return type;
    }

    /**
     * @return the licence plate
     */
    public String getPlateNumber() {
        return plateNumber;
    }

    /**
     * @return the load capacity in kilograms, or {@code NaN} when none is
     *         recorded
     */
    public double getCapacityKg() {
        return capacityKg;
    }

    /**
     * A one-line label for dialogs and table cells.
     *
     * @return text of the form {@code V001 - CAB-1234 (VAN)}
     */
    public String getDisplayInfo() {
        return vehicleId + " - " + plateNumber + " (" + type + ")";
    }

    @Override
    public String toString() {
        return getDisplayInfo();
    }
}
