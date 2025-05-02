package gui;

import main.DatabaseConnector;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class SupplyView extends JFrame {
    private JTable supplyTable;
    private MainView mainFrame;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public SupplyView(MainView mainFrame) {
        this.mainFrame = mainFrame;
        setTitle("Управление поставками");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        supplyTable = new JTable();
        refreshSupplyTable();

        JButton addButton = new JButton("Добавить поставку");
        addButton.addActionListener(e -> showAddSupplyDialog());

        setLayout(new BorderLayout());
        add(new JScrollPane(supplyTable), BorderLayout.CENTER);
        add(addButton, BorderLayout.SOUTH);
    }

    public void createSupplyManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Заголовок
        JLabel titleLabel = new JLabel("Управление поставками", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Панель с таблицей и кнопками
        JPanel mainContentPanel = new JPanel(new BorderLayout());

        // Таблица поставок
        supplyTable = new JTable();
        refreshSupplyTable();
        JScrollPane scrollPane = new JScrollPane(supplyTable);
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

    public void refreshSupplyTable() {
        try {
            String sql = "SELECT supply_id, supply_date, supplier_name, notes FROM supplies ORDER BY supply_date DESC";

            Statement statement = DatabaseConnector.getConnection().createStatement();
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

            supplyTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
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

    public void showAddSupplyDialog() {
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
                PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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
                            refreshSupplyTable();
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

    public void showSupplyComponentsDialog() {
        int selectedRow = supplyTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Пожалуйста, выберите поставку.",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int supplyId = (Integer) supplyTable.getValueAt(selectedRow, 0);

        JDialog dialog = new JDialog(mainFrame, "Компоненты поставки #" + supplyId, true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(mainFrame);

        try {
            String sql = "SELECT sc.supply_component_id, c.name, c.type, sc.quantity, c.description " +
                    "FROM supply_components sc " +
                    "JOIN components c ON sc.component_id = c.component_id " +
                    "WHERE sc.supply_id = ?";

            PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql);
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

    public void showAddComponentToSupplyDialog() {
        int selectedRow = supplyTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Пожалуйста, выберите поставку.",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int supplyId = (Integer) supplyTable.getValueAt(selectedRow, 0);
        showAddComponentToExistingSupplyDialog(supplyId);
    }

    public void showAddComponentToExistingSupplyDialog(int supplyId) {
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
                PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql);
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

    private void increaseComponentQuantity(int componentId, int quantity) throws SQLException {
        String sql = "UPDATE components SET quantity = quantity + ? WHERE component_id = ?";
        PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql);
        statement.setInt(1, quantity);
        statement.setInt(2, componentId);
        statement.executeUpdate();
        statement.close();

//        refreshInventoryTable();
    }
}