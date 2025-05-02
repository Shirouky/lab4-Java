package main;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnector {
    private static final String DB_URL = "jdbc:h2:./database/olivanders;AUTO_SERVER=TRUE";
    private static final String DB_USER = "olivander";
    private static final String DB_PASSWORD = "password";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public void createTables() {
        // Таблица компонентов
        try (Connection conn = getConnection();

             Statement stmt = conn.createStatement()) {
            // Таблица компонентов
            stmt.execute("CREATE TABLE IF NOT EXISTS components (" +
                    "component_id INT AUTO_INCREMENT PRIMARY KEY NOT NULL," +
                    "type VARCHAR(10) NOT NULL CHECK (type IN ('wood', 'core'))," +
                    "name VARCHAR(100) NOT NULL," +
                    "description TEXT," +
                    "quantity INT DEFAULT 0," +
                    "CONSTRAINT unique_component UNIQUE (type, name))");

            // Таблица поставок
            stmt.execute("CREATE TABLE IF NOT EXISTS supplies (" +
                    "supply_id INT AUTO_INCREMENT PRIMARY KEY NOT NULL," +
                    "supply_date DATE NOT NULL," +
                    "supplier_name VARCHAR(100)," +
                    "notes TEXT)");

            // Таблица связи поставок и компонентов
            stmt.execute("CREATE TABLE IF NOT EXISTS supply_components (" +
                    "supply_component_id INT PRIMARY KEY NOT NULL," +
                    "supply_id INT NOT NULL," +
                    "component_id INT NOT NULL," +
                    "quantity INT NOT NULL," +
                    "FOREIGN KEY (supply_id) REFERENCES supplies(supply_id) ON DELETE CASCADE," +
                    "FOREIGN KEY (component_id) REFERENCES components(component_id))");

            // Таблица покупателей
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "customer_id INT AUTO_INCREMENT PRIMARY KEY NOT NULL," +
                    "first_name VARCHAR(50) NOT NULL," +
                    "last_name VARCHAR(50) NOT NULL," +
                    "birth_date DATE," +
                    "magic_school VARCHAR(100)," +
                    "registration_date DATE NOT NULL)");

            // Таблица палочек
            stmt.execute("CREATE TABLE IF NOT EXISTS wands (" +
                    "wand_id INT PRIMARY KEY NOT NULL," +
                    "wood_id INT NOT NULL," +
                    "core_id INT NOT NULL," +
                    "length DOUBLE," +
                    "flexibility VARCHAR(50)," +
                    "manufacture_date DATE NOT NULL," +
                    "price DOUBLE NOT NULL," +
                    "status VARCHAR(10) NOT NULL CHECK (status IN ('available', 'sold'))," +
                    "customer_id INT," +
                    "sale_date DATE," +
                    "FOREIGN KEY (wood_id) REFERENCES components(component_id)," +
                    "FOREIGN KEY (core_id) REFERENCES components(component_id)," +
                    "FOREIGN KEY (customer_id) REFERENCES customers(customer_id))");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Ошибка инициализации БД: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}