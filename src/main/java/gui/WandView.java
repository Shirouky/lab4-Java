package gui;

import main.DatabaseConnector;

import javax.swing.*;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class WandView extends JFrame {
    private MainView mainFrame;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public WandView(MainView mainFrame) {
        this.mainFrame = mainFrame;
        initializeUI();
    }

    public void initializeUI() {
        setTitle("Управление палочками");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Создание интерфейса...
        JButton backButton = new JButton("Назад");
        backButton.addActionListener(e -> {
            this.dispose();
            mainFrame.setVisible(true);
        });

        add(new JScrollPane(createWandsTable()), BorderLayout.CENTER);
        add(backButton, BorderLayout.SOUTH);
    }

    public JTable createWandsTable() {
        // Создание таблицы с данными
        return new JTable();
    }

    public void refreshWandsTable() {
        try {
            String sql = "SELECT w.wand_id, w1.name AS wood, w2.name AS core, w.length, w.flexibility, " +
                    "w.manufacture_date, w.price, w.status, " +
                    "CONCAT(c.first_name, ' ', c.last_name) AS customer, w.sale_date " +
                    "FROM wands w " +
                    "JOIN components w1 ON w.wood_id = w1.component_id " +
                    "JOIN components w2 ON w.core_id = w2.component_id " +
                    "LEFT JOIN customers c ON w.customer_id = c.customer_id " +
                    "ORDER BY w.status, w.wand_id";

            Statement statement = DatabaseConnector.getConnection().createStatement();
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

    // ========== Управление палочками ==========
    public JTable wandsTable;
    public JComboBox<String> woodComboBox, coreComboBox;

    public void createWandManagementPanel() {
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

    public void showAddWandDialog() {
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

                PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql);
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


    public void decreaseComponentQuantity(int componentId, int quantity) throws SQLException {
        String sql = "UPDATE components SET quantity = quantity - ? WHERE component_id = ?";
        PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql);
        statement.setInt(1, quantity);
        statement.setInt(2, componentId);
        statement.executeUpdate();
        statement.close();

//        refreshInventoryTable();
    }

    public void loadComponentsToComboBox(JComboBox<String> comboBox, String type) {
        comboBox.removeAllItems();
        try {
            String sql = "SELECT component_id, name FROM components WHERE type = ? ORDER BY name";
            PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql);
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

    public void sellSelectedWand() {
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
//        refreshCustomersTable(customersTable);
        JScrollPane scrollPane = new JScrollPane(customersTable);
        customerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel customerButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton newCustomerButton = new JButton("Новый покупатель");
        newCustomerButton.addActionListener(e -> {
            dialog.dispose();
//            int customerId = showAddCustomerDialog();
//            if (customerId > 0) {
//                completeWandSale(wandId, customerId);
//            }
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

    public void completeWandSale(int wandId, int customerId) {
        try {
            String sql = "UPDATE wands SET status = 'sold', customer_id = ?, sale_date = CURDATE() WHERE wand_id = ?";
            PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql);
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

    public void showSearchWandsDialog() {
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

                PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql.toString());

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
}