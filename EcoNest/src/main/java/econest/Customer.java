package econest;

/** Represents one row from the clients table. */
public class Customer {
    private final String clientId;
    private final String name;
    private final String clientType;
    private final String address;
    private final String phone;
    private final String email;
    private final String companyName;

    public Customer(String clientId, String name, String clientType,
                    String address, String phone, String email, String companyName) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required.");
        }
        if (!"HOUSEHOLD".equals(clientType) && !"OFFICE".equals(clientType)) {
            throw new IllegalArgumentException("Customer type must be HOUSEHOLD or OFFICE.");
        }
        if (email != null && !email.isBlank()
                && !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email address.");
        }
        if (phone != null && !phone.isBlank() && !phone.matches("[0-9+() -]{7,20}")) {
            throw new IllegalArgumentException("Invalid phone number.");
        }
        if ("OFFICE".equals(clientType) && (companyName == null || companyName.isBlank())) {
            throw new IllegalArgumentException("Company name is required for OFFICE clients.");
        }
        this.clientId = clientId;
        this.name = name.trim();
        this.clientType = clientType;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.companyName = "HOUSEHOLD".equals(clientType) ? null : companyName;
    }

    public String getClientId() { return clientId; }
    public String getName() { return name; }
    public String getClientType() { return clientType; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getCompanyName() { return companyName; }
}
