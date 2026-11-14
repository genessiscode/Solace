package econest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Database operations for the MySQL clients table. */
public class CustomerDAO {
    public List<Customer> getAll(String search, String type) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT client_id, name, client_type, address, phone, email, company_name " +
            "FROM clients WHERE 1=1");
        List<Object> values = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (client_id LIKE ? OR name LIKE ? OR phone LIKE ? OR email LIKE ? OR company_name LIKE ?)");
            String term = "%" + search.trim() + "%";
            for (int i = 0; i < 5; i++) values.add(term);
        }
        if (type != null && !type.equals("ALL")) {
            sql.append(" AND client_type = ?");
            values.add(type);
        }
        sql.append(" ORDER BY client_id");

        List<Customer> customers = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < values.size(); i++) ps.setObject(i + 1, values.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    customers.add(new Customer(
                        rs.getString("client_id"), rs.getString("name"),
                        rs.getString("client_type"), rs.getString("address"),
                        rs.getString("phone"), rs.getString("email"),
                        rs.getString("company_name")));
                }
            }
        }
        return customers;
    }

    public Customer findById(String clientId) throws SQLException {
        String sql = "SELECT client_id, name, client_type, address, phone, email, company_name " +
                     "FROM clients WHERE client_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Customer(
                    rs.getString("client_id"), rs.getString("name"),
                    rs.getString("client_type"), rs.getString("address"),
                    rs.getString("phone"), rs.getString("email"),
                    rs.getString("company_name"));
            }
        }
    }

    public String create(Customer customer) throws SQLException {
        String sql = "INSERT INTO clients (client_id, name, client_type, address, phone, email, company_name) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String newId = nextCustomerId(con);
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    bindCustomer(ps, customer, newId);
                    ps.executeUpdate();
                }
                con.commit();
                return newId;
            } catch (SQLException | RuntimeException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public void update(Customer customer) throws SQLException {
        String sql = "UPDATE clients SET name=?, client_type=?, address=?, phone=?, email=?, company_name=? " +
                     "WHERE client_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getClientType());
            ps.setString(3, emptyToNull(customer.getAddress()));
            ps.setString(4, emptyToNull(customer.getPhone()));
            ps.setString(5, emptyToNull(customer.getEmail()));
            ps.setString(6, emptyToNull(customer.getCompanyName()));
            ps.setString(7, customer.getClientId());
            if (ps.executeUpdate() == 0) throw new SQLException("Customer was not found.");
        }
    }

    public void delete(String clientId) throws SQLException {
        String sql = "DELETE FROM clients WHERE client_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, clientId);
            if (ps.executeUpdate() == 0) throw new SQLException("Customer was not found.");
        }
    }

    private String nextCustomerId(Connection con) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(client_id, 2) AS UNSIGNED)), 0) + 1 " +
                     "FROM clients WHERE client_id REGEXP '^C[0-9]+$' FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            rs.next();
            return String.format("C%03d", rs.getInt(1));
        }
    }

    private void bindCustomer(PreparedStatement ps, Customer c, String id) throws SQLException {
        ps.setString(1, id);
        ps.setString(2, c.getName());
        ps.setString(3, c.getClientType());
        ps.setString(4, emptyToNull(c.getAddress()));
        ps.setString(5, emptyToNull(c.getPhone()));
        ps.setString(6, emptyToNull(c.getEmail()));
        ps.setString(7, emptyToNull(c.getCompanyName()));
    }

    private static String emptyToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
