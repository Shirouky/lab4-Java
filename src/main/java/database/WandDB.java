package database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class WandDB {
    public Vector<Vector<Object>> getAll() throws SQLException {
        Vector<Vector<Object>> data = new Vector<>();
        String sql = "SELECT w.wand_id, w1.name AS wood, w2.name AS core, w.length, w.flexibility, " +
                "w.price, w.status, " +
                "CONCAT(c.first_name, ' ', c.last_name) AS customer, w.sale_date " +
                "FROM wands w " +
                "JOIN components w1 ON w.wood_id = w1.component_id " +
                "JOIN components w2 ON w.core_id = w2.component_id " +
                "LEFT JOIN customers c ON w.customer_id = c.customer_id " +
                "ORDER BY w.status, w.wand_id";

        Statement statement = DatabaseConnector.getConnection().createStatement();
        ResultSet result = statement.executeQuery(sql);

        while (result.next()) {
            Vector<Object> row = new Vector<>();
            row.add(result.getInt("wand_id"));
            row.add(result.getString("wood"));
            row.add(result.getString("core"));
            row.add(result.getDouble("length"));
            row.add(result.getString("flexibility"));
            row.add(result.getDouble("price"));
            row.add(result.getString("status").equals("available") ? "В наличии" : "Продана");
            row.add(result.getString("customer") != null ? result.getString("customer") : "-");
            row.add(result.getDate("sale_date") != null ? result.getDate("sale_date") : "-");
            data.add(row);
        }

        result.close();
        statement.close();
        return data;
    }

    public Vector<Vector<Object>> get(int customerId) throws SQLException {
        String sql = "SELECT w.wand_id, w1.name AS wood, w2.name AS core, w.length, w.flexibility, " +
                "w.price, w.sale_date " +
                "FROM wands w " +
                "JOIN components w1 ON w.wood_id = w1.component_id " +
                "JOIN components w2 ON w.core_id = w2.component_id " +
                "WHERE w.customer_id = ? " +
                "ORDER BY w.sale_date DESC";

        PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql);
        statement.setInt(1, customerId);

        ResultSet result = statement.executeQuery();
        Vector<Vector<Object>> data = new Vector<>();

        while (result.next()) {
            Vector<Object> row = new Vector<>();
            row.add(result.getInt("wand_id"));
            row.add(result.getString("wood"));
            row.add(result.getString("core"));
            row.add(result.getDouble("length"));
            row.add(result.getString("flexibility"));
            row.add(result.getDouble("price"));
            row.add(result.getDate("sale_date"));
            data.add(row);
        }
        return data;
    }

    public void sale(int wandId, int customerId) throws SQLException {
        String sql = "UPDATE wands SET status = 'sold', customer_id = ?, sale_date = CURDATE() WHERE wand_id = ?";
        PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql);
        statement.setInt(1, customerId);
        statement.setInt(2, wandId);

        statement.executeUpdate();
        statement.close();
    }

    public void create(int woodId, int coreId, double length, String flexibility, double price) throws SQLException {
        ComponentDB componentController = new ComponentDB();
        if (componentController.check(coreId) && componentController.check(woodId)) {
            String sql = "INSERT INTO wands (wood_id, core_id, length, flexibility, price) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql)) {
                statement.setInt(1, woodId);
                statement.setInt(2, coreId);
                statement.setDouble(3, length);
                statement.setString(4, flexibility);
                statement.setDouble(5, price);

                statement.executeUpdate();
            }

            componentController.decrease(woodId, 1);
            componentController.decrease(coreId, 1);
        } else {
            throw new SQLException("недостаточно компонентов для создания палочки");
        }
    }
}
