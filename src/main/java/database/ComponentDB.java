package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

public class ComponentDB {
    public Vector<Vector<Object>> getAll() throws SQLException {
        Vector<Vector<Object>> data = new Vector<>();
        String sql = "SELECT component_id, name, type, quantity FROM components";

        ResultSet result;
        try (Statement statement = DatabaseConnector.getConnection().createStatement()) {
            result = statement.executeQuery(sql);

            while (result.next()) {
                Vector<Object> row = new Vector<>();
                row.add(result.getInt("component_id"));
                row.add(result.getString("name"));
                row.add(result.getString("type").equals("wood") ? "Древесина" : "Сердцевина");
                row.add(result.getInt("quantity"));
                data.add(row);
            }
        }
        return data;
    }

    public ArrayList<String> get(String type) throws SQLException {
        ArrayList<String> data = new ArrayList<>();
        String sql = "SELECT component_id, name FROM components WHERE type = ?";
        ResultSet result;
        try (PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql)) {
            statement.setString(1, type);
            result = statement.executeQuery();

            while (result.next()) {
                data.add(result.getInt("component_id") + " - " + result.getString("name"));
            }
        }
        return data;
    }

    public Vector<Vector<Object>> getComponentsSupply(int supplyId) throws SQLException {
        Vector<Vector<Object>> data = new Vector<>();
        String sql = "SELECT sc.supply_component_id, c.name, c.type, sc.quantity " +
                "FROM supply_components sc " +
                "JOIN components c ON sc.component_id = c.component_id " +
                "WHERE sc.supply_id = ?";

        ResultSet result;
        try (PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql)) {
            statement.setInt(1, supplyId);

            result = statement.executeQuery();
            while (result.next()) {
                Vector<Object> row = new Vector<>();
                row.add(result.getInt("supply_component_id"));
                row.add(result.getString("name"));
                row.add(result.getString("type").equals("wood") ? "Древесина" : "Сердцевина");
                row.add(result.getInt("quantity"));
                data.add(row);
            }
        }
        return data;
    }

    public void decrease(int componentId) throws SQLException {
        String sql = "UPDATE components SET quantity = quantity - ? WHERE component_id = ?";
        PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql);
        statement.setInt(1, 1);
        statement.setInt(2, componentId);
        statement.executeUpdate();
        statement.close();
    }

    public void increase(int componentId, int quantity) throws SQLException {
        String sql = "UPDATE components SET quantity = quantity + ? WHERE component_id = ?";
        PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql);
        statement.setInt(1, quantity);
        statement.setInt(2, componentId);
        statement.executeUpdate();
        statement.close();
    }

    public boolean check(int componentId) throws SQLException {
        String sql = "SELECT quantity FROM components WHERE component_id = ?";
        ResultSet result;
        try (PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql)) {
            statement.setInt(1, componentId);
            result = statement.executeQuery();

            if (result.next()) {
                if (result.getInt("quantity") > 0) {
                    return true;
                }
            }
        }
        return false;
    }

    public void create(int supplyId, int componentId, int quantity) throws SQLException {
        String sql = "INSERT INTO supply_components (supply_id, component_id, quantity) VALUES (?, ?, ?)";
        try (PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql)) {
            statement.setInt(1, supplyId);
            statement.setInt(2, componentId);
            statement.setInt(3, quantity);

            statement.executeUpdate();
        }
        increase(componentId, quantity);
    }
}
