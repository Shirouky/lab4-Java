package gui;

import database.Controller;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class CustomerView extends JFrame {
    private MainView mainFrame;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private final Controller controller;
    public JTable customersTable;

    public CustomerView(MainView mainFrame, CardLayout cardLayout, JPanel cardPanel) {
        this.mainFrame = mainFrame;
        setTitle("Управление покупателями");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        controller = new Controller();
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        this.mainFrame = mainFrame;
    }

    public void createPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Управление покупателями", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel mainContentPanel = new JPanel(new BorderLayout());

        customersTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(customersTable);
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton addCustomerButton = new JButton("Добавить покупателя");
        addCustomerButton.addActionListener(e -> showAddCustomerDialog());
        buttonPanel.add(addCustomerButton);

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

    public void refreshTable() {
        Vector<Vector<Object>> data;
        try {
            data = controller.getAllCustomers();
            Vector<String> columnNames = new Vector<>();

            columnNames.add("ID");
            columnNames.add("Имя");
            columnNames.add("Фамилия");
            columnNames.add("Школа магии");

            customersTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при загрузке данных: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showAddCustomerDialog() {
        JDialog dialog = new JDialog(mainFrame, "Добавление нового покупателя", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Имя:"));
        JTextField firstNameField = new JTextField();
        panel.add(firstNameField);

        panel.add(new JLabel("Фамилия:"));
        JTextField lastNameField = new JTextField();
        panel.add(lastNameField);

        panel.add(new JLabel("Школа магии:"));
        JTextField magicSchoolField = new JTextField();
        panel.add(magicSchoolField);

        // Кнопки
        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(e -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String magicSchool = magicSchoolField.getText();

            if (firstName.isEmpty() || lastName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Имя и фамилия обязательны для заполнения!",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                controller.createCustomer(firstName, lastName, magicSchool);
                JOptionPane.showMessageDialog(dialog, "Новый покупатель успешно добавлен",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                dialog.dispose();
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
    }

    public void showCustomerWandsDialog() {
        int selectedRow = customersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Пожалуйста, выберите покупателя.",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int customerId = (Integer) customersTable.getValueAt(selectedRow, 0);
        String customerName = customersTable.getValueAt(selectedRow, 1) + " " +
                customersTable.getValueAt(selectedRow, 2);

        JDialog dialog = new JDialog(mainFrame, "Палочки покупателя " + customerName, true);
        dialog.setSize(800, 400);
        dialog.setLocationRelativeTo(mainFrame);

        try {
            Vector<String> columnNames = new Vector<>();

            columnNames.add("ID");
            columnNames.add("Древесина");
            columnNames.add("Сердцевина");
            columnNames.add("Длина");
            columnNames.add("Гибкость");
            columnNames.add("Цена");

            Vector<Vector<Object>> data = controller.getWand(customerId);

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
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при загрузке данных: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}