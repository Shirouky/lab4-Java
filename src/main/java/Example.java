import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;


public class Example {
    static final String DB_URL = "jdbc:h2:./database/olivanders;" +
            "MODE=MySQL;" +  // Режим совместимости с MySQL
            "DATABASE_TO_UPPER=false";
    private static final String DB_USER = "olivander";
    private static final String DB_PASSWORD = "password";

    private static JFrame mainFrame;
    private static CardLayout cardLayout;
    private static JPanel cardPanel;

    public static void main(String[] args) {
        // Инициализация базы данных
        createTables();

        // Создание главного окна
        createMainWindow();
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    private static void createTables() {
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

    private static void createMainWindow() {
        mainFrame = new JFrame("Магазин волшебных палочек Олливандеры");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);
        mainFrame.setLocationRelativeTo(null);

        // Создаем панель с CardLayout для переключения между экранами
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Создаем экраны (панели) для разных функций
        createMainMenuPanel();
        createWandManagementPanel();
        createCustomerManagementPanel();
        createSupplyManagementPanel();
        createInventoryPanel();

        // Добавляем панель карт в главное окно
        mainFrame.add(cardPanel);

        // Показываем главное меню при запуске
        cardLayout.show(cardPanel, "MainMenu");

        mainFrame.setVisible(true);
    }

    private static void createMainMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Заголовок
        JLabel titleLabel = new JLabel("Магазин волшебных палочек Олливандеры", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Кнопки меню
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));

        JButton wandsButton = new JButton("Управление палочками");
        wandsButton.addActionListener(e -> cardLayout.show(cardPanel, "WandManagement"));
        buttonPanel.add(wandsButton);

        JButton customersButton = new JButton("Управление покупателями");
        customersButton.addActionListener(e -> cardLayout.show(cardPanel, "CustomerManagement"));
        buttonPanel.add(customersButton);

        JButton suppliesButton = new JButton("Управление поставками");
        suppliesButton.addActionListener(e -> cardLayout.show(cardPanel, "SupplyManagement"));
        buttonPanel.add(suppliesButton);

        JButton inventoryButton = new JButton("Просмотр состояния склада");
        inventoryButton.addActionListener(e -> {
            refreshInventoryTable();
            cardLayout.show(cardPanel, "Inventory");
        });
        buttonPanel.add(inventoryButton);

        JButton clearDataButton = new JButton("Очистить все данные");
        clearDataButton.addActionListener(e -> clearAllData());
        buttonPanel.add(clearDataButton);

        panel.add(buttonPanel, BorderLayout.CENTER);

        cardPanel.add(panel, "MainMenu");
    }

    // ========== Управление палочками ==========
    private static JTable wandsTable;
    private static JComboBox<String> woodComboBox, coreComboBox;

    private static void createWandManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Заголовок
        JLabel titleLabel = new JLabel("Управление палочками", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Панель с таблицей и кнопками
        JPanel mainContentPanel = new JPanel(new BorderLayout());

        // Таблица палочек
        wandsTable = new JTable();
        refreshWandsTable();
        JScrollPane scrollPane = new JScrollPane(wandsTable);
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton addWandButton = new JButton("Добавить палочку");
        addWandButton.addActionListener(e -> showAddWandDialog());
        buttonPanel.add(addWandButton);

        JButton sellWandButton = new JButton("Продать палочку");
        sellWandButton.addActionListener(e -> sellSelectedWand());
        buttonPanel.add(sellWandButton);

        JButton searchWandButton = new JButton("Поиск палочек");
        searchWandButton.addActionListener(e -> showSearchWandsDialog());
        buttonPanel.add(searchWandButton);

        JButton backButton = new JButton("Назад");
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "MainMenu"));
        buttonPanel.add(backButton);

        mainContentPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(mainContentPanel, BorderLayout.CENTER);

        cardPanel.add(panel, "WandManagement");
    }

    private static void refreshWandsTable() {
        try {
            String sql = "SELECT w.wand_id, w1.name AS wood, w2.name AS core, w.length, w.flexibility, " +
                    "w.manufacture_date, w.price, w.status, " +
                    "CONCAT(c.first_name, ' ', c.last_name) AS customer, w.sale_date " +
                    "FROM wands w " +
                    "JOIN components w1 ON w.wood_id = w1.component_id " +
                    "JOIN components w2 ON w.core_id = w2.component_id " +
                    "LEFT JOIN customers c ON w.customer_id = c.customer_id " +
                    "ORDER BY w.status, w.wand_id";

            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(sql);

            // Создаем модель таблицы
            Vector<Vector<Object>> data = new Vector<>();
            Vector<String> columnNames = new Vector<>();

            // Заголовки столбцов
            columnNames.add("ID");
            columnNames.add("Древесина");
            columnNames.add("Сердцевина");
            columnNames.add("Длина");
            columnNames.add("Гибкость");
            columnNames.add("Дата изг.");
            columnNames.add("Цена");
            columnNames.add("Статус");
            columnNames.add("Покупатель");
            columnNames.add("Дата продажи");

            // Данные
            while (result.next()) {
                Vector<Object> row = new Vector<>();
                row.add(result.getInt("wand_id"));
                row.add(result.getString("wood"));
                row.add(result.getString("core"));
                row.add(result.getDouble("length"));
                row.add(result.getString("flexibility"));
                row.add(result.getDate("manufacture_date"));
                row.add(result.getDouble("price"));
                row.add(result.getString("status").equals("available") ? "В наличии" : "Продана");
                row.add(result.getString("customer") != null ? result.getString("customer") : "-");
                row.add(result.getDate("sale_date") != null ? result.getDate("sale_date") : "-");
                data.add(row);
            }

            wandsTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });

            result.close();
            statement.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при загрузке данных: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showAddWandDialog() {
        JDialog dialog = new JDialog(mainFrame, "Добавление новой палочки", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Древесина
        panel.add(new JLabel("Древесина:"));
        woodComboBox = new JComboBox<>();
        loadComponentsToComboBox(woodComboBox, "wood");
        panel.add(woodComboBox);

        // Сердцевина
        panel.add(new JLabel("Сердцевина:"));
        coreComboBox = new JComboBox<>();
        loadComponentsToComboBox(coreComboBox, "core");
        panel.add(coreComboBox);

        // Длина
        panel.add(new JLabel("Длина (дюймы):"));
        JTextField lengthField = new JTextField();
        panel.add(lengthField);

        // Гибкость
        panel.add(new JLabel("Гибкость:"));
        JTextField flexibilityField = new JTextField();
        panel.add(flexibilityField);

        // Дата изготовления
        panel.add(new JLabel("Дата изготовления:"));
        JTextField manufactureDateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        panel.add(manufactureDateField);

        // Цена
        panel.add(new JLabel("Цена:"));
        JTextField priceField = new JTextField();
        panel.add(priceField);

        // Кнопки
        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(e -> {
            try {
                int woodId = Integer.parseInt(woodComboBox.getSelectedItem().toString().split(" - ")[0]);
                int coreId = Integer.parseInt(coreComboBox.getSelectedItem().toString().split(" - ")[0]);
                double length = Double.parseDouble(lengthField.getText());
                String flexibility = flexibilityField.getText();
                String manufactureDate = manufactureDateField.getText();
                double price = Double.parseDouble(priceField.getText());

                String sql = "INSERT INTO wands (wood_id, core_id, length, flexibility, manufacture_date, price) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

                PreparedStatement statement = getConnection().prepareStatement(sql);
                statement.setInt(1, woodId);
                statement.setInt(2, coreId);
                statement.setDouble(3, length);
                statement.setString(4, flexibility);
                statement.setString(5, manufactureDate);
                statement.setDouble(6, price);

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(dialog, "Новая палочка успешно добавлена!",
                            "Успех", JOptionPane.INFORMATION_MESSAGE);

                    // Уменьшение количества компонентов на складе
                    decreaseComponentQuantity(woodId, 1);
                    decreaseComponentQuantity(coreId, 1);

                    refreshWandsTable();
                    dialog.dispose();
                }

                statement.close();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, введите корректные числовые значения.",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Ошибка при добавлении палочки: " + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private static void loadComponentsToComboBox(JComboBox<String> comboBox, String type) {
        comboBox.removeAllItems();
        try {
            String sql = "SELECT component_id, name FROM components WHERE type = ? ORDER BY name";
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setString(1, type);
            ResultSet result = statement.executeQuery();

            while (result.next()) {
                comboBox.addItem(result.getInt("component_id") + " - " + result.getString("name"));
            }

            result.close();
            statement.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при загрузке компонентов: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void sellSelectedWand() {
        int selectedRow = wandsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Пожалуйста, выберите палочку для продажи.",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int wandId = (Integer) wandsTable.getValueAt(selectedRow, 0);
        String status = (String) wandsTable.getValueAt(selectedRow, 7);

        if (!status.equals("В наличии")) {
            JOptionPane.showMessageDialog(mainFrame, "Эта палочка уже продана!",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Диалог выбора покупателя
        JDialog dialog = new JDialog(mainFrame, "Продажа палочки", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Выбор покупателя
        JPanel customerPanel = new JPanel(new BorderLayout());
        customerPanel.add(new JLabel("Выберите покупателя:"), BorderLayout.NORTH);

        JTable customersTable = new JTable();
        refreshCustomersTable(customersTable);
        JScrollPane scrollPane = new JScrollPane(customersTable);
        customerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel customerButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton newCustomerButton = new JButton("Новый покупатель");
        newCustomerButton.addActionListener(e -> {
            dialog.dispose();
            int customerId = showAddCustomerDialog();
            if (customerId > 0) {
                completeWandSale(wandId, customerId);
            }
        });
        customerButtonPanel.add(newCustomerButton);
        customerPanel.add(customerButtonPanel, BorderLayout.SOUTH);

        panel.add(customerPanel, BorderLayout.CENTER);

        // Кнопки
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton sellButton = new JButton("Продать");
        sellButton.addActionListener(e -> {
            int selectedCustomerRow = customersTable.getSelectedRow();
            if (selectedCustomerRow == -1) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, выберите покупателя.",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int customerId = (Integer) customersTable.getValueAt(selectedCustomerRow, 0);
            dialog.dispose();
            completeWandSale(wandId, customerId);
        });

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(sellButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private static void completeWandSale(int wandId, int customerId) {
        try {
            String sql = "UPDATE wands SET status = 'sold', customer_id = ?, sale_date = CURDATE() WHERE wand_id = ?";
            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, customerId);
            statement.setInt(2, wandId);

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(mainFrame, "Палочка успешно продана!",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
                refreshWandsTable();
            }

            statement.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при продаже палочки: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showSearchWandsDialog() {
        JDialog dialog = new JDialog(mainFrame, "Поиск палочек", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Поля для поиска
        panel.add(new JLabel("Тип древесины:"));
        JTextField woodField = new JTextField();
        panel.add(woodField);

        panel.add(new JLabel("Тип сердцевины:"));
        JTextField coreField = new JTextField();
        panel.add(coreField);

        panel.add(new JLabel("Минимальная длина:"));
        JTextField minLengthField = new JTextField();
        panel.add(minLengthField);

        panel.add(new JLabel("Максимальная длина:"));
        JTextField maxLengthField = new JTextField();
        panel.add(maxLengthField);

        panel.add(new JLabel("Гибкость:"));
        JTextField flexibilityField = new JTextField();
        panel.add(flexibilityField);

        panel.add(new JLabel("Статус:"));
        JComboBox<String> statusComboBox = new JComboBox<>(new String[]{"", "available", "sold"});
        panel.add(statusComboBox);

        // Кнопки
        JButton searchButton = new JButton("Поиск");
        searchButton.addActionListener(e -> {
            String woodType = woodField.getText();
            String coreType = coreField.getText();
            String minLengthStr = minLengthField.getText();
            String maxLengthStr = maxLengthField.getText();
            String flexibility = flexibilityField.getText();
            String status = (String) statusComboBox.getSelectedItem();

            try {
                StringBuilder sql = new StringBuilder("SELECT w.wand_id, w1.name AS wood, w2.name AS core, w.length, " +
                        "w.flexibility, w.manufacture_date, w.price, w.status " +
                        "FROM wands w " +
                        "JOIN components w1 ON w.wood_id = w1.component_id " +
                        "JOIN components w2 ON w.core_id = w2.component_id " +
                        "WHERE 1=1");

                if (!woodType.isEmpty()) {
                    sql.append(" AND w1.name LIKE ?");
                }
                if (!coreType.isEmpty()) {
                    sql.append(" AND w2.name LIKE ?");
                }
                if (!minLengthStr.isEmpty()) {
                    sql.append(" AND w.length >= ?");
                }
                if (!maxLengthStr.isEmpty()) {
                    sql.append(" AND w.length <= ?");
                }
                if (!flexibility.isEmpty()) {
                    sql.append(" AND w.flexibility LIKE ?");
                }
                if (!status.isEmpty()) {
                    sql.append(" AND w.status = ?");
                }

                sql.append(" ORDER BY w.wand_id");

                PreparedStatement statement = getConnection().prepareStatement(sql.toString());

                int paramIndex = 1;
                if (!woodType.isEmpty()) {
                    statement.setString(paramIndex++, "%" + woodType + "%");
                }
                if (!coreType.isEmpty()) {
                    statement.setString(paramIndex++, "%" + coreType + "%");
                }
                if (!minLengthStr.isEmpty()) {
                    statement.setDouble(paramIndex++, Double.parseDouble(minLengthStr));
                }
                if (!maxLengthStr.isEmpty()) {
                    statement.setDouble(paramIndex++, Double.parseDouble(maxLengthStr));
                }
                if (!flexibility.isEmpty()) {
                    statement.setString(paramIndex++, "%" + flexibility + "%");
                }
                if (!status.isEmpty()) {
                    statement.setString(paramIndex++, status);
                }

                ResultSet result = statement.executeQuery();

                // Создаем модель таблицы
                Vector<Vector<Object>> data = new Vector<>();
                Vector<String> columnNames = new Vector<>();

                // Заголовки столбцов
                columnNames.add("ID");
                columnNames.add("Древесина");
                columnNames.add("Сердцевина");
                columnNames.add("Длина");
                columnNames.add("Гибкость");
                columnNames.add("Дата изг.");
                columnNames.add("Цена");
                columnNames.add("Статус");

                // Данные
                while (result.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(result.getInt("wand_id"));
                    row.add(result.getString("wood"));
                    row.add(result.getString("core"));
                    row.add(result.getDouble("length"));
                    row.add(result.getString("flexibility"));
                    row.add(result.getDate("manufacture_date"));
                    row.add(result.getDouble("price"));
                    row.add(result.getString("status").equals("available") ? "В наличии" : "Продана");
                    data.add(row);
                }

                // Показываем результаты в новом окне
                JDialog resultDialog = new JDialog(dialog, "Результаты поиска", true);
                resultDialog.setSize(800, 400);
                resultDialog.setLocationRelativeTo(dialog);

                JTable resultTable = new JTable(data, columnNames);
                JScrollPane resultScrollPane = new JScrollPane(resultTable);
                resultDialog.add(resultScrollPane);

                JButton closeButton = new JButton("Закрыть");
                closeButton.addActionListener(ev -> resultDialog.dispose());

                JPanel buttonPanel = new JPanel();
                buttonPanel.add(closeButton);

                resultDialog.add(buttonPanel, BorderLayout.SOUTH);
                resultDialog.setVisible(true);

                result.close();
                statement.close();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, введите корректные числовые значения для длины.",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Ошибка при поиске: " + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(searchButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ========== Управление покупателями ==========
    private static JTable customersTable;

    private static void createCustomerManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Заголовок
        JLabel titleLabel = new JLabel("Управление покупателями", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Панель с таблицей и кнопками
        JPanel mainContentPanel = new JPanel(new BorderLayout());

        // Таблица покупателей
        customersTable = new JTable();
        refreshCustomersTable(customersTable);
        JScrollPane scrollPane = new JScrollPane(customersTable);
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton addCustomerButton = new JButton("Добавить покупателя");
        addCustomerButton.addActionListener(e -> showAddCustomerDialog());
        buttonPanel.add(addCustomerButton);

        JButton searchCustomerButton = new JButton("Поиск по имени");
        searchCustomerButton.addActionListener(e -> showSearchCustomerDialog());
        buttonPanel.add(searchCustomerButton);

        JButton viewWandsButton = new JButton("Просмотр палочек");
        viewWandsButton.addActionListener(e -> showCustomerWandsDialog());
        buttonPanel.add(viewWandsButton);

        JButton backButton = new JButton("Назад");
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "MainMenu"));
        buttonPanel.add(backButton);

        mainContentPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(mainContentPanel, BorderLayout.CENTER);

        cardPanel.add(panel, "CustomerManagement");
    }

    private static void refreshCustomersTable(JTable table) {
        try {
            String sql = "SELECT customer_id, first_name, last_name, birth_date, magic_school, registration_date " +
                    "FROM customers ORDER BY last_name, first_name";

            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(sql);

            // Создаем модель таблицы
            Vector<Vector<Object>> data = new Vector<>();
            Vector<String> columnNames = new Vector<>();

            // Заголовки столбцов
            columnNames.add("ID");
            columnNames.add("Имя");
            columnNames.add("Фамилия");
            columnNames.add("Дата рожд.");
            columnNames.add("Школа магии");
            columnNames.add("Дата регистр.");

            // Данные
            while (result.next()) {
                Vector<Object> row = new Vector<>();
                row.add(result.getInt("customer_id"));
                row.add(result.getString("first_name"));
                row.add(result.getString("last_name"));
                row.add(result.getDate("birth_date") != null ? result.getDate("birth_date") : "-");
                row.add(result.getString("magic_school") != null ? result.getString("magic_school") : "-");
                row.add(result.getDate("registration_date"));
                data.add(row);
            }

            table.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });

            result.close();
            statement.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при загрузке данных: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static int showAddCustomerDialog() {
        JDialog dialog = new JDialog(mainFrame, "Добавление нового покупателя", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Поля для ввода
        panel.add(new JLabel("Имя:"));
        JTextField firstNameField = new JTextField();
        panel.add(firstNameField);

        panel.add(new JLabel("Фамилия:"));
        JTextField lastNameField = new JTextField();
        panel.add(lastNameField);

        panel.add(new JLabel("Дата рождения (ГГГГ-ММ-ДД):"));
        JTextField birthDateField = new JTextField();
        panel.add(birthDateField);

        panel.add(new JLabel("Школа магии:"));
        JTextField magicSchoolField = new JTextField();
        panel.add(magicSchoolField);

        // Кнопки
        JButton addButton = new JButton("Добавить");
        final int[] customerId = {0};
        addButton.addActionListener(e -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String birthDateStr = birthDateField.getText();
            String magicSchool = magicSchoolField.getText();

            if (firstName.isEmpty() || lastName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Имя и фамилия обязательны для заполнения!",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                String sql = "INSERT INTO customers (first_name, last_name, birth_date, magic_school, registration_date) " +
                        "VALUES (?, ?, ?, ?, CURDATE())";

                PreparedStatement statement = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                statement.setString(1, firstName);
                statement.setString(2, lastName);

                if (!birthDateStr.isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        java.util.Date utilDate = sdf.parse(birthDateStr);
                        statement.setDate(3, new java.sql.Date(utilDate.getTime()));
                    } catch (ParseException ex) {
                        JOptionPane.showMessageDialog(dialog, "Неверный формат даты. Используйте ГГГГ-ММ-ДД.",
                                "Ошибка", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else {
                    statement.setNull(3, Types.DATE);
                }

                if (!magicSchool.isEmpty()) {
                    statement.setString(4, magicSchool);
                } else {
                    statement.setNull(4, Types.VARCHAR);
                }

                int rowsInserted = statement.executeUpdate();

                if (rowsInserted > 0) {
                    ResultSet generatedKeys = statement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        customerId[0] = generatedKeys.getInt(1);
                        JOptionPane.showMessageDialog(dialog, "Новый покупатель успешно добавлен с ID: " + customerId[0],
                                "Успех", JOptionPane.INFORMATION_MESSAGE);

                        refreshCustomersTable(customersTable);
                        dialog.dispose();
                    }
                    generatedKeys.close();
                }

                statement.close();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, введите дату в формате ГГГГ-ММ-ДД.",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Ошибка при добавлении покупателя: " + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);

        return customerId[0];
    }

    private static void showSearchCustomerDialog() {
        JDialog dialog = new JDialog(mainFrame, "Поиск покупателя", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Поле для поиска
        panel.add(new JLabel("Введите часть имени или фамилии:"), BorderLayout.NORTH);
        JTextField searchField = new JTextField();
        panel.add(searchField, BorderLayout.CENTER);

        // Кнопки
        JButton searchButton = new JButton("Поиск");
        searchButton.addActionListener(e -> {
            String searchTerm = searchField.getText();
            if (searchTerm.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, введите имя для поиска.",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                String sql = "SELECT customer_id, first_name, last_name, birth_date, magic_school " +
                        "FROM customers " +
                        "WHERE first_name LIKE ? OR last_name LIKE ? " +
                        "ORDER BY last_name, first_name";

                PreparedStatement statement = getConnection().prepareStatement(sql);
                statement.setString(1, "%" + searchTerm + "%");
                statement.setString(2, "%" + searchTerm + "%");

                ResultSet result = statement.executeQuery();

                // Создаем модель таблицы
                Vector<Vector<Object>> data = new Vector<>();
                Vector<String> columnNames = new Vector<>();

                // Заголовки столбцов
                columnNames.add("ID");
                columnNames.add("Имя");
                columnNames.add("Фамилия");
                columnNames.add("Дата рожд.");
                columnNames.add("Школа магии");

                // Данные
                while (result.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(result.getInt("customer_id"));
                    row.add(result.getString("first_name"));
                    row.add(result.getString("last_name"));
                    row.add(result.getDate("birth_date") != null ? result.getDate("birth_date") : "-");
                    row.add(result.getString("magic_school") != null ? result.getString("magic_school") : "-");
                    data.add(row);
                }

                // Показываем результаты в новом окне
                JDialog resultDialog = new JDialog(dialog, "Результаты поиска", true);
                resultDialog.setSize(600, 300);
                resultDialog.setLocationRelativeTo(dialog);

                JTable resultTable = new JTable(data, columnNames);
                JScrollPane resultScrollPane = new JScrollPane(resultTable);
                resultDialog.add(resultScrollPane);

                JButton closeButton = new JButton("Закрыть");
                closeButton.addActionListener(ev -> resultDialog.dispose());

                JPanel buttonPanel = new JPanel();
                buttonPanel.add(closeButton);

                resultDialog.add(buttonPanel, BorderLayout.SOUTH);
                resultDialog.setVisible(true);

                result.close();
                statement.close();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Ошибка при поиске: " + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(searchButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private static void showCustomerWandsDialog() {
        int selectedRow = customersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Пожалуйста, выберите покупателя.",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int customerId = (Integer) customersTable.getValueAt(selectedRow, 0);
        String customerName = (String) customersTable.getValueAt(selectedRow, 1) + " " +
                (String) customersTable.getValueAt(selectedRow, 2);

        JDialog dialog = new JDialog(mainFrame, "Палочки покупателя " + customerName, true);
        dialog.setSize(800, 400);
        dialog.setLocationRelativeTo(mainFrame);

        try {
            String sql = "SELECT w.wand_id, w1.name AS wood, w2.name AS core, w.length, w.flexibility, " +
                    "w.manufacture_date, w.price, w.sale_date " +
                    "FROM wands w " +
                    "JOIN components w1 ON w.wood_id = w1.component_id " +
                    "JOIN components w2 ON w.core_id = w2.component_id " +
                    "WHERE w.customer_id = ? " +
                    "ORDER BY w.sale_date DESC";

            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, customerId);

            ResultSet result = statement.executeQuery();

            // Создаем модель таблицы
            Vector<Vector<Object>> data = new Vector<>();
            Vector<String> columnNames = new Vector<>();

            // Заголовки столбцов
            columnNames.add("ID");
            columnNames.add("Древесина");
            columnNames.add("Сердцевина");
            columnNames.add("Длина");
            columnNames.add("Гибкость");
            columnNames.add("Дата изг.");
            columnNames.add("Цена");
            columnNames.add("Дата покупки");

            // Данные
            while (result.next()) {
                Vector<Object> row = new Vector<>();
                row.add(result.getInt("wand_id"));
                row.add(result.getString("wood"));
                row.add(result.getString("core"));
                row.add(result.getDouble("length"));
                row.add(result.getString("flexibility"));
                row.add(result.getDate("manufacture_date"));
                row.add(result.getDouble("price"));
                row.add(result.getDate("sale_date"));
                data.add(row);
            }

            if (data.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Этот покупатель еще не приобрел ни одной палочки.",
                        "Информация", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                return;
            }

            JTable wandsTable = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(wandsTable);
            dialog.add(scrollPane);

            JButton closeButton = new JButton("Закрыть");
            closeButton.addActionListener(e -> dialog.dispose());

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);

            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);

            result.close();
            statement.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при загрузке данных: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ========== Управление поставками ==========
    private static JTable suppliesTable;

    private static void createSupplyManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Заголовок
        JLabel titleLabel = new JLabel("Управление поставками", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Панель с таблицей и кнопками
        JPanel mainContentPanel = new JPanel(new BorderLayout());

        // Таблица поставок
        suppliesTable = new JTable();
        refreshSuppliesTable();
        JScrollPane scrollPane = new JScrollPane(suppliesTable);
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton addSupplyButton = new JButton("Добавить поставку");
        addSupplyButton.addActionListener(e -> showAddSupplyDialog());
        buttonPanel.add(addSupplyButton);

        JButton viewComponentsButton = new JButton("Просмотр компонентов");
        viewComponentsButton.addActionListener(e -> showSupplyComponentsDialog());
        buttonPanel.add(viewComponentsButton);

        JButton addComponentButton = new JButton("Добавить компонент");
        addComponentButton.addActionListener(e -> showAddComponentToSupplyDialog());
        buttonPanel.add(addComponentButton);

        JButton backButton = new JButton("Назад");
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "MainMenu"));
        buttonPanel.add(backButton);

        mainContentPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(mainContentPanel, BorderLayout.CENTER);

        cardPanel.add(panel, "SupplyManagement");
    }

    private static void refreshSuppliesTable() {
        try {
            String sql = "SELECT supply_id, supply_date, supplier_name, notes FROM supplies ORDER BY supply_date DESC";

            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(sql);

            // Создаем модель таблицы
            Vector<Vector<Object>> data = new Vector<>();
            Vector<String> columnNames = new Vector<>();

            // Заголовки столбцов
            columnNames.add("ID");
            columnNames.add("Дата");
            columnNames.add("Поставщик");
            columnNames.add("Примечания");

            // Данные
            while (result.next()) {
                Vector<Object> row = new Vector<>();
                row.add(result.getInt("supply_id"));
                row.add(result.getDate("supply_date"));
                row.add(result.getString("supplier_name") != null ? result.getString("supplier_name") : "-");
                row.add(result.getString("notes") != null ? result.getString("notes") : "-");
                data.add(row);
            }

            suppliesTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });

            result.close();
            statement.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при загрузке данных: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showAddSupplyDialog() {
        JDialog dialog = new JDialog(mainFrame, "Добавление новой поставки", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Поля для ввода
        panel.add(new JLabel("Дата поставки:"));
        JTextField dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        panel.add(dateField);

        panel.add(new JLabel("Поставщик:"));
        JTextField supplierField = new JTextField();
        panel.add(supplierField);

        panel.add(new JLabel("Примечания:"));
        JTextField notesField = new JTextField();
        panel.add(notesField);

        // Кнопки
        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(e -> {
            String dateStr = dateField.getText();
            String supplier = supplierField.getText();
            String notes = notesField.getText();

            try {
                String sql = "INSERT INTO supplies (supply_date, supplier_name, notes) VALUES (?, ?, ?)";
                PreparedStatement statement = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    java.util.Date utilDate = sdf.parse(dateStr);
                    statement.setDate(1, new java.sql.Date(utilDate.getTime()));
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(dialog, "Неверный формат даты. Используйте ГГГГ-ММ-ДД.",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!supplier.isEmpty()) {
                    statement.setString(2, supplier);
                } else {
                    statement.setNull(2, Types.VARCHAR);
                }

                if (!notes.isEmpty()) {
                    statement.setString(3, notes);
                } else {
                    statement.setNull(3, Types.VARCHAR);
                }

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    ResultSet generatedKeys = statement.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int supplyId = generatedKeys.getInt(1);
                        JOptionPane.showMessageDialog(dialog, "Новая поставка успешно добавлена с ID: " + supplyId,
                                "Успех", JOptionPane.INFORMATION_MESSAGE);

                        // Предложение добавить компоненты
                        int option = JOptionPane.showConfirmDialog(dialog,
                                "Хотите добавить компоненты в эту поставку?",
                                "Добавление компонентов", JOptionPane.YES_NO_OPTION);

                        if (option == JOptionPane.YES_OPTION) {
                            dialog.dispose();
                            showAddComponentToExistingSupplyDialog(supplyId);
                        } else {
                            refreshSuppliesTable();
                            dialog.dispose();
                        }
                    }
                    generatedKeys.close();
                }

                statement.close();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, введите дату в формате ГГГГ-ММ-ДД.",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Ошибка при добавлении поставки: " + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private static void showSupplyComponentsDialog() {
        int selectedRow = suppliesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Пожалуйста, выберите поставку.",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int supplyId = (Integer) suppliesTable.getValueAt(selectedRow, 0);

        JDialog dialog = new JDialog(mainFrame, "Компоненты поставки #" + supplyId, true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(mainFrame);

        try {
            String sql = "SELECT sc.supply_component_id, c.name, c.type, sc.quantity, c.description " +
                    "FROM supply_components sc " +
                    "JOIN components c ON sc.component_id = c.component_id " +
                    "WHERE sc.supply_id = ?";

            PreparedStatement statement = getConnection().prepareStatement(sql);
            statement.setInt(1, supplyId);

            ResultSet result = statement.executeQuery();

            // Создаем модель таблицы
            Vector<Vector<Object>> data = new Vector<>();
            Vector<String> columnNames = new Vector<>();

            // Заголовки столбцов
            columnNames.add("ID");
            columnNames.add("Название");
            columnNames.add("Тип");
            columnNames.add("Количество");
            columnNames.add("Описание");

            // Данные
            while (result.next()) {
                Vector<Object> row = new Vector<>();
                row.add(result.getInt("supply_component_id"));
                row.add(result.getString("name"));
                row.add(result.getString("type").equals("wood") ? "Древесина" : "Сердцевина");
                row.add(result.getInt("quantity"));
                row.add(result.getString("description") != null ? result.getString("description") : "-");
                data.add(row);
            }

            if (data.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "В этой поставке нет компонентов.",
                        "Информация", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                return;
            }

            JTable componentsTable = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(componentsTable);
            dialog.add(scrollPane);

            JButton closeButton = new JButton("Закрыть");
            closeButton.addActionListener(e -> dialog.dispose());

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);

            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);

            result.close();
            statement.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при загрузке данных: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showAddComponentToSupplyDialog() {
        int selectedRow = suppliesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Пожалуйста, выберите поставку.",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int supplyId = (Integer) suppliesTable.getValueAt(selectedRow, 0);
        showAddComponentToExistingSupplyDialog(supplyId);
    }

    private static void showAddComponentToExistingSupplyDialog(int supplyId) {
        JDialog dialog = new JDialog(mainFrame, "Добавление компонента в поставку #" + supplyId, true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Выбор типа компонента
        panel.add(new JLabel("Тип компонента:"));
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Древесина", "Сердцевина"});
        panel.add(typeComboBox);

        // Выбор компонента
        panel.add(new JLabel("Компонент:"));
        JComboBox<String> componentComboBox = new JComboBox<>();
        panel.add(componentComboBox);

        // Обновление списка компонентов при изменении типа
        typeComboBox.addActionListener(e -> {
            String type = typeComboBox.getSelectedItem().equals("Древесина") ? "wood" : "core";
            loadComponentsToComboBox(componentComboBox, type);
        });

        // Инициализация списка компонентов
        loadComponentsToComboBox(componentComboBox, "wood");

        // Количество
        panel.add(new JLabel("Количество:"));
        JTextField quantityField = new JTextField();
        panel.add(quantityField);

        // Кнопки
        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(e -> {
            try {
                int componentId = Integer.parseInt(componentComboBox.getSelectedItem().toString().split(" - ")[0]);
                int quantity = Integer.parseInt(quantityField.getText());

                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Количество должно быть положительным числом.",
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String sql = "INSERT INTO supply_components (supply_id, component_id, quantity) VALUES (?, ?, ?)";
                PreparedStatement statement = getConnection().prepareStatement(sql);
                statement.setInt(1, supplyId);
                statement.setInt(2, componentId);
                statement.setInt(3, quantity);

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(dialog, "Компонент успешно добавлен в поставку!",
                            "Успех", JOptionPane.INFORMATION_MESSAGE);

                    // Увеличение количества компонентов на складе
                    increaseComponentQuantity(componentId, quantity);

                    dialog.dispose();
                }

                statement.close();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, введите корректное количество.",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Ошибка при добавлении компонента: " + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ========== Просмотр состояния склада ==========
    private static JTable inventoryTable;

    private static void createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Заголовок
        JLabel titleLabel = new JLabel("Состояние склада", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Таблица инвентаря
        inventoryTable = new JTable();
        refreshInventoryTable();
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Кнопка "Назад"
        JButton backButton = new JButton("Назад");
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "MainMenu"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(backButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        cardPanel.add(panel, "Inventory");
    }

    private static void refreshInventoryTable() {
        try {
            String sql = "SELECT component_id, name, type, description, quantity " +
                    "FROM components ORDER BY type, name";

            Statement statement = getConnection().createStatement();
            ResultSet result = statement.executeQuery(sql);

            // Создаем модель таблицы
            Vector<Vector<Object>> data = new Vector<>();
            Vector<String> columnNames = new Vector<>();

            // Заголовки столбцов
            columnNames.add("ID");
            columnNames.add("Название");
            columnNames.add("Тип");
            columnNames.add("Описание");
            columnNames.add("Количество");

            // Данные
            while (result.next()) {
                Vector<Object> row = new Vector<>();
                row.add(result.getInt("component_id"));
                row.add(result.getString("name"));
                row.add(result.getString("type").equals("wood") ? "Древесина" : "Сердцевина");
                row.add(result.getString("description") != null ? result.getString("description") : "-");
                row.add(result.getInt("quantity"));
                data.add(row);
            }

            inventoryTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });

            result.close();
            statement.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при загрузке данных: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ========== Вспомогательные методы ==========
    private static void increaseComponentQuantity(int componentId, int quantity) throws SQLException {
        String sql = "UPDATE components SET quantity = quantity + ? WHERE component_id = ?";
        PreparedStatement statement = getConnection().prepareStatement(sql);
        statement.setInt(1, quantity);
        statement.setInt(2, componentId);
        statement.executeUpdate();
        statement.close();

        refreshInventoryTable();
    }

    private static void decreaseComponentQuantity(int componentId, int quantity) throws SQLException {
        String sql = "UPDATE components SET quantity = quantity - ? WHERE component_id = ?";
        PreparedStatement statement = getConnection().prepareStatement(sql);
        statement.setInt(1, quantity);
        statement.setInt(2, componentId);
        statement.executeUpdate();
        statement.close();

        refreshInventoryTable();
    }

    private static void clearAllData() {
        int confirm = JOptionPane.showConfirmDialog(mainFrame,
                "Вы уверены, что хотите полностью очистить все данные?\nЭто действие нельзя отменить.",
                "Подтверждение очистки", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Statement statement = getConnection().createStatement();

                // Отключаем проверку внешних ключей для очистки таблиц
                statement.execute("SET FOREIGN_KEY_CHECKS = 0");

                // Очищаем таблицы в правильном порядке
                statement.execute("TRUNCATE TABLE wands");
                statement.execute("TRUNCATE TABLE customers");
                statement.execute("TRUNCATE TABLE supply_components");
                statement.execute("TRUNCATE TABLE supplies");
                statement.execute("TRUNCATE TABLE components");

                // Включаем проверку внешних ключей обратно
                statement.execute("SET FOREIGN_KEY_CHECKS = 1");

                statement.close();

                JOptionPane.showMessageDialog(mainFrame, "Все данные успешно очищены.",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);

                // Обновляем все таблицы
                refreshWandsTable();
                refreshCustomersTable(customersTable);
                refreshSuppliesTable();
                refreshInventoryTable();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(mainFrame, "Ошибка при очистке данных: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}