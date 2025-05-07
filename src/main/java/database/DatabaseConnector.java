package database;

import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;

import javax.swing.*;
import java.sql.*;

public class DatabaseConnector {
    private static final String DB_URL = "jdbc:h2:./database/ollivanders;AUTO_SERVER=TRUE";
    private static final String DB_USER = "ollivander";
    private static final String DB_PASSWORD = "alohomora";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public void createTables() {
        try (Connection conn = getConnection();

             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS components (" +
                    "component_id INT AUTO_INCREMENT PRIMARY KEY NOT NULL," +
                    "type VARCHAR(10) NOT NULL CHECK (type IN ('wood', 'core'))," +
                    "name VARCHAR(100) NOT NULL," +
                    "quantity INT DEFAULT 0," +
                    "CONSTRAINT unique_component UNIQUE (type, name))");

            stmt.execute("CREATE TABLE IF NOT EXISTS supplies (" +
                    "supply_id INT AUTO_INCREMENT PRIMARY KEY NOT NULL," +
                    "supply_date DATE NOT NULL," +
                    "supplier_name VARCHAR(100))");

            stmt.execute("CREATE TABLE IF NOT EXISTS supply_components (" +
                    "supply_component_id INT AUTO_INCREMENT PRIMARY KEY NOT NULL," +
                    "supply_id INT NOT NULL," +
                    "component_id INT NOT NULL," +
                    "quantity INT NOT NULL," +
                    "FOREIGN KEY (supply_id) REFERENCES supplies(supply_id) ON DELETE CASCADE," +
                    "FOREIGN KEY (component_id) REFERENCES components(component_id))");

            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "customer_id INT AUTO_INCREMENT PRIMARY KEY NOT NULL," +
                    "first_name VARCHAR(50) NOT NULL," +
                    "last_name VARCHAR(50) NOT NULL," +
                    "magic_school VARCHAR(100))");

            stmt.execute("CREATE TABLE IF NOT EXISTS wands (" +
                    "wand_id INT AUTO_INCREMENT PRIMARY KEY NOT NULL," +
                    "wood_id INT NOT NULL," +
                    "core_id INT NOT NULL," +
                    "length DOUBLE," +
                    "flexibility VARCHAR(50)," +
                    "price DOUBLE NOT NULL," +
                    "status VARCHAR(10) DEFAULT 'available' CHECK (status IN ('available', 'sold'))," +
                    "customer_id INT," +
                    "sale_date DATE," +
                    "FOREIGN KEY (wood_id) REFERENCES components(component_id)," +
                    "FOREIGN KEY (core_id) REFERENCES components(component_id)," +
                    "FOREIGN KEY (customer_id) REFERENCES customers(customer_id))");

            createComponents();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Ошибка инициализации БД: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void createComponents() throws SQLException {
        try {
            String sql = "INSERT INTO components (type, name)" +
                    "VALUES (?, ?)";

            try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
                statement.setString(1, "wood");
                statement.setString(2, "Боярышник");
                statement.executeUpdate();
            }
            try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
                statement.setString(1, "wood");
                statement.setString(2, "Остролист");
                statement.executeUpdate();
            }
            try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
                statement.setString(1, "wood");
                statement.setString(2, "Тис");
                statement.executeUpdate();
            }
            try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
                statement.setString(1, "wood");
                statement.setString(2, "Вяз");
                statement.executeUpdate();
            }
            try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
                statement.setString(1, "wood");
                statement.setString(2, "Бузина");
                statement.executeUpdate();
            }


            try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
                statement.setString(1, "core");
                statement.setString(2, "Волос единорога");
                statement.executeUpdate();
            }
            try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
                statement.setString(1, "core");
                statement.setString(2, "Перо феникса");
                statement.executeUpdate();
            }
            try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
                statement.setString(1, "core");
                statement.setString(2, "Сердечная жила дракона");
                statement.executeUpdate();
            }
            try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
                statement.setString(1, "core");
                statement.setString(2, "Рог рогатого змея");
                statement.executeUpdate();
            }
            try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
                statement.setString(1, "core");
                statement.setString(2, "Волос вейлы");
                statement.executeUpdate();
            }
        } catch (JdbcSQLIntegrityConstraintViolationException e) {
            System.out.println();
        }
    }

    public static void clearAll() throws SQLException {
        try (Statement statement = DatabaseConnector.getConnection().createStatement()) {
            statement.execute("DELETE FROM wands");
            statement.execute("DELETE FROM supplies");
            statement.execute("DELETE FROM customers");
            statement.execute("DELETE FROM components");
            statement.execute("DELETE FROM supply_components");
        }
    }
}