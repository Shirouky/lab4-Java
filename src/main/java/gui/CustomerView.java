package gui;

import main.DatabaseConnector;
import objects.Customer;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;

public class CustomerView extends JFrame {
    private final JTable customerTable;
    private MainView mainFrame;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    public CustomerView(MainView mainFrame) {
        this.mainFrame = mainFrame;
        setTitle("Управление покупателями");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        customerTable = new JTable();

        JButton addButton = new JButton("Добавить покупателя");
        addButton.addActionListener(e -> showAddCustomerDialog());

        setLayout(new BorderLayout());
        add(new JScrollPane(customerTable), BorderLayout.CENTER);
        add(addButton, BorderLayout.SOUTH);
    }


    // ========== Управление покупателями ==========
    public JTable customersTable;

    public void createCustomerManagementPanel() {
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

    public void refreshCustomersTable(JTable table) {
        try {
            String sql = "SELECT customer_id, first_name, last_name, birth_date, magic_school, registration_date " +
                    "FROM customers ORDER BY last_name, first_name";

            Statement statement = DatabaseConnector.getConnection().createStatement();
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

    public int showAddCustomerDialog() {
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

                PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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

    public void showSearchCustomerDialog() {
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

                PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql);
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

    public void showCustomerWandsDialog() {
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

            PreparedStatement statement = DatabaseConnector.getConnection().prepareStatement(sql);
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

}