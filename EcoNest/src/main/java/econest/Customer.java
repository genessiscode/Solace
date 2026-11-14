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
        this.clientId = clientId;
        this.name = name;
        this.clientType = clientType;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.companyName = companyName;
    }

    public String getClientId() { return clientId; }
    public String getName() { return name; }
    public String getClientType() { return clientType; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getCompanyName() { return companyName; }
}
