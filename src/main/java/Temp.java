import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;


public class Temp {
    static final String DB_URL = "jdbc:h2:./database/olivanders;" +
            "MODE=MySQL;" +  // Режим совместимости с MySQL
            "DATABASE_TO_UPPER=false";
    private static final String DB_USER = "olivander";
    private static final String DB_PASSWORD = "password";

    private static JFrame mainFrame;
    private static CardLayout cardLayout;
    private static JPanel cardPanel;

    public static void main(String[] args) {
//        // Инициализация базы данных
//        createTables();
//
//        // Создание главного окна
//        createMainWindow();
    }




    // ========== Управление поставками ==========

    // ========== Просмотр состояния склада ==========

}