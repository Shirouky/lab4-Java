package database;

import java.sql.*;
import java.util.Vector;

public class CustomerDB {
    public Vector<Vector<Object>> getAll() throws SQLException {
        Vector<Vector<Object>> data = new Vector<>();
        String sql = "SELECT customer_id, first_name, last_name, magic_school " +
                "FROM customers ORDER BY last_name, first_name";

        ResultSet result;
        try (Statement statement = DatabaseConnector.getConnection().createStatement()) {
            result = statement.executeQuery(sql);
            while (result.next()) {
                Vector<Object> row = new Vector<>();
                row.add(result.getInt("customer_id"));
                row.add(result.getString("first_name"));
                row.add(result.getString("last_name"));
                row.add(result.getString("magic_school") != null ? result.getString("magic_school") : "-");
                data.add(row);
            }
        }

        return data;
    }

    public void create(String firstName, String lastName, String magicSchool) throws SQLException {
        String sql = "INSERT INTO customers (first_name, last_name, magic_school) " +
                "VALUES (?, ?, ?)";

        try (PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, firstName);
            statement.setString(2, lastName);

            if (!magicSchool.isEmpty()) {
                statement.setString(3, magicSchool);
            } else {
                statement.setNull(3, Types.VARCHAR);
            }

            statement.executeUpdate();
        }

    }
}
