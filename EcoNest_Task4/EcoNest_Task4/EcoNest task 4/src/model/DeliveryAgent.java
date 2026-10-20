package model;

public class DeliveryAgent {
    private String agentId;
    private String name;
    private String phone;
    private String address;
    private String vehicleNumber;
    private String vehicleType;
    private String status;

    public DeliveryAgent(String agentId, String name, String phone,
                         String address, String vehicleNumber,
                         String vehicleType, String status) {
        this.agentId = agentId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.status = status;
    }

    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
