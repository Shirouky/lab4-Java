package gui;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class CustomerView extends View implements ViewInterface {
    public CustomerView(MainView mainFrame, CardLayout cardLayout, JPanel cardPanel) {
        super(mainFrame, cardLayout, cardPanel);

        columnNames.add("ID");
        columnNames.add("Имя");
        columnNames.add("Фамилия");
        columnNames.add("Школа магии");
    }

    @Override
    public void createPanel() {
        createPanel("Управление покупателями", createButtonPanel(), "CustomerManagement");
        refreshTable();
    }

    @Override
    public JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton addCustomerButton = new JButton("Добавить покупателя");
        addCustomerButton.addActionListener(e -> showAddCustomerDialog());
        buttonPanel.add(addCustomerButton);

        JButton viewWandsButton = new JButton("Просмотр палочек");
        viewWandsButton.addActionListener(e -> showCustomerDialog());
        buttonPanel.add(viewWandsButton);

        JButton backButton = new JButton("Назад");
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "MainMenu"));
        buttonPanel.add(backButton);
        return buttonPanel;
    }

    @Override
    public void refreshTable() {
        try {
            Vector<Vector<Object>> data = controller.getAllCustomers();
            refreshTable(data, columnNames, table);
        } catch (SQLException e) {
            messageError(mainFrame, "Ошибка при загрузке данных: " + e.getMessage());
        }
    }

    public JTable getTable() {
        return table;
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

        JButton addButton = addButton(dialog, firstNameField, lastNameField, magicSchoolField);

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public JButton addButton(JDialog dialog, JTextField firstNameField, JTextField lastNameField, JTextField magicSchoolField) {
        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(e -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String magicSchool = magicSchoolField.getText();

            if (firstName.isEmpty() || lastName.isEmpty()) {
                messageError(dialog, "Имя и фамилия обязательны для заполнения!");
                return;
            }

            try {
                controller.createCustomer(firstName, lastName, magicSchool);
                messageSuccess(dialog, "Новый покупатель успешно добавлен");
                refreshTable();
                dialog.dispose();
            } catch (SQLException ex) {
                messageError(dialog, "Ошибка при добавлении покупателя: " + ex.getMessage());
            }
        });
        return addButton;
    }

    public void showCustomerDialog() {
        int customerId = super.selectRow(table);
        if (customerId == -1) return;

        JDialog dialog = new JDialog(mainFrame, "Палочки покупателя", true);
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
            String message = "Этот покупатель еще не приобрел ни одной палочки.";
            popupTable(data, columnNames, message, dialog);
        } catch (SQLException e) {
            messageError(mainFrame, "Ошибка при загрузке данных: " + e.getMessage());
        }
    }
}