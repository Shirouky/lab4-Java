package database;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Vector;

public class SupplyDB {
    public Vector<Vector<Object>> getAll() throws SQLException {
        Vector<Vector<Object>> data = new Vector<>();
        String sql = "SELECT supply_id, supply_date, supplier_name FROM supplies ORDER BY supply_date DESC";

        Statement statement = DatabaseConnector.getConnection().createStatement();
        ResultSet result = statement.executeQuery(sql);

        while (result.next()) {
            Vector<Object> row = new Vector<>();
            row.add(result.getInt("supply_id"));
            row.add(result.getDate("supply_date"));
            row.add(result.getString("supplier_name") != null ? result.getString("supplier_name") : "-");
            data.add(row);
        }

        result.close();
        statement.close();
        return data;
    }

    public void create(String date, String supplier) throws SQLException, ParseException {
        String sql = "INSERT INTO supplies (supply_date, supplier_name) VALUES (?, ?)";
        try (PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date utilDate = sdf.parse(date);
            statement.setDate(1, new java.sql.Date(utilDate.getTime()));

            if (!supplier.isEmpty()) {
                statement.setString(2, supplier);
            } else {
                statement.setNull(2, Types.VARCHAR);
            }
            statement.executeUpdate();
        }
    }
}
