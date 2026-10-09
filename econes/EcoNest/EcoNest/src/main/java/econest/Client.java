package econest;

/**
 * One EcoNest client - either a household or an office.
 *
 * <p>{@code clientType} is the {@link ClientType} enum rather than a
 * {@code String} so that {@link ClientManager} can map the
 * {@code clients.client_type} column without a second, competing
 * representation of the same two values.</p>
 *
 * <p>The id and name are validated on construction and trimmed, so an
 * invalid client can never reach the database. This mirrors {@link Agent},
 * which validates the same two required fields. Optional columns
 * ({@code phone}, {@code email}, {@code companyName}) are normalised to an
 * empty string rather than {@code null}, so getters can be used directly in
 * the UI without null checks.</p>
 */
public class Client {
    private final String clientId;
    private final String name;
    private final ClientType clientType;
    private final String address;
    private final String phone;
    private final String email;
    private final String companyName;

    /**
     * @param clientId    the client identifier, for example {@code C001}
     * @param name        the display name of the client
     * @param clientType  whether the client is a household or an office
     * @param address     the optional postal address
     * @param phone       the optional phone number
     * @param email       the optional email address
     * @param companyName the optional company name, used by office clients
     * @throws IllegalArgumentException if {@code clientId} or {@code name}
     *                                  is blank, or {@code clientType} is
     *                                  {@code null}
     */
    public Client(String clientId, String name, ClientType clientType,
                  String address, String phone, String email, String companyName) {

        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Client ID cannot be empty.");
        }

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Client name cannot be empty.");
        }

        if (clientType == null) {
            throw new IllegalArgumentException(
                    "Client type cannot be empty.");
        }

        this.clientId = clientId.trim();
        this.name = name.trim();
        this.clientType = clientType;
        this.address = address == null ? "" : address.trim();
        this.phone = phone == null ? "" : phone.trim();
        this.email = email == null ? "" : email.trim();
        this.companyName = companyName == null ? "" : companyName.trim();
    }

    public String getClientId() { return clientId; }
    public String getName() { return name; }
    public ClientType getClientType() { return clientType; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getCompanyName() { return companyName; }
}
