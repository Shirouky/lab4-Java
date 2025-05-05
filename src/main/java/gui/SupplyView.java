package gui;

import main.Controller;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.Vector;

public class SupplyView extends View implements ViewInterface{
    public SupplyView(MainView mainFrame, CardLayout cardLayout, JPanel cardPanel) {
        super(mainFrame, cardLayout, cardPanel);

        columnNames.add("ID");
        columnNames.add("Дата");
        columnNames.add("Поставщик");
    }

    @Override
    public void createPanel() {
        createPanel("Управление поставками", createButtonPanel(), "SupplyManagement");
        refreshTable();
    }

    @Override
    public JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton addSupplyButton = new JButton("Добавить поставку");
        addSupplyButton.addActionListener(e -> showAddSupplyDialog());
        buttonPanel.add(addSupplyButton);

        JButton viewComponentsButton = new JButton("Просмотр компонентов");
        viewComponentsButton.addActionListener(e -> showSupplyComponentsDialog());
        buttonPanel.add(viewComponentsButton);

        JButton addComponentButton = new JButton("Добавить компонент");
        addComponentButton.addActionListener(e -> showAddComponentDialog());
        buttonPanel.add(addComponentButton);

        JButton backButton = new JButton("Назад");
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "MainMenu"));
        buttonPanel.add(backButton);
        return buttonPanel;
    }

    @Override
    public void refreshTable() {
        try {
            Vector<Vector<Object>> data = controller.getAllSupplies();
            refreshTable(data, columnNames, table);
        } catch (SQLException e) {
            messageError(mainFrame, "Ошибка при загрузке данных: " + e.getMessage());
        }
    }

    public void showAddSupplyDialog() {
        JDialog dialog = new JDialog(mainFrame, "Добавление новой поставки", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Дата поставки:"));
        JTextField dateField = new JTextField(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        panel.add(dateField);

        panel.add(new JLabel("Поставщик:"));
        JTextField supplierField = new JTextField();
        panel.add(supplierField);

        JButton addButton = addButton(dateField, supplierField, dialog, controller);

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JButton addButton(JTextField dateField, JTextField supplierField, JDialog dialog, Controller controller) {
        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(e -> {
            String dateStr = dateField.getText();
            String supplier = supplierField.getText();
            try {
                controller.createSupply(dateStr, supplier);
                messageSuccess(dialog, "Новая поставка успешно добавлена");
                refreshTable();
                dialog.dispose();

            } catch (IllegalArgumentException | ParseException ex) {
                messageError(dialog, "Пожалуйста, введите дату в формате ГГГГ-ММ-ДД.");
            } catch (SQLException ex) {
                messageError(dialog, "Ошибка при добавлении поставки: " + ex.getMessage());
            }
        });
        return addButton;
    }

    public void showSupplyComponentsDialog() {
        int supplyId = super.selectRow(table);
        if (supplyId == -1) return;

        JDialog dialog = new JDialog(mainFrame, "Компоненты поставки #" + supplyId, true);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(mainFrame);
        try {
            Vector<String> columnNames = new Vector<>();

            columnNames.add("ID");
            columnNames.add("Название");
            columnNames.add("Тип");
            columnNames.add("Количество");

            Vector<Vector<Object>> data = controller.getComponentsSupply(supplyId);
            String message = "В этой поставке нет компонентов.";
            popupTable(data, columnNames, message, dialog);
        } catch (SQLException e) {
            messageError(mainFrame, "Ошибка при загрузке данных: " + e.getMessage());
        }
    }

    public void showAddComponentDialog() {
        int supplyId = super.selectRow(table);
        if (supplyId == -1) return;

        JDialog dialog = new JDialog(mainFrame, "Добавление компонента в поставку #" + supplyId, true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(new JLabel("Тип компонента:"));
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Древесина", "Сердцевина"});
        panel.add(typeComboBox);

        panel.add(new JLabel("Компонент:"));
        JComboBox<String> componentComboBox = new JComboBox<>();
        panel.add(componentComboBox);

        typeComboBox.addActionListener(e -> {
            String type = Objects.equals(typeComboBox.getSelectedItem(), "Древесина") ? "wood" : "core";
            loadComponents(componentComboBox, type);
        });

        loadComponents(componentComboBox, "wood");

        panel.add(new JLabel("Количество:"));
        JFormattedTextField quantityField = new JFormattedTextField(NumberFormat.getNumberInstance());
        panel.add(quantityField);

        JButton addButton = addButtonComponent(dialog, componentComboBox, quantityField, supplyId);

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public JButton addButtonComponent(JDialog dialog, JComboBox<String> componentComboBox, JFormattedTextField quantityField, int supplyId) {
        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(e -> {
            try {
                int componentId = Integer.parseInt(Objects.requireNonNull(componentComboBox.getSelectedItem()).toString().split(" - ")[0]);
                int quantity = Integer.parseInt(quantityField.getText());

                if (quantity <= 0) {
                    messageError(dialog, "Количество должно быть положительным числом.");
                }

                controller.createComponents(supplyId, componentId, quantity);

                messageSuccess(dialog, "Компонент успешно добавлен в поставку!");
                dialog.dispose();

            } catch (NumberFormatException ex) {
                messageError(dialog, "Пожалуйста, введите корректное количество.");
            } catch (SQLException ex) {
                messageError(dialog, "Ошибка при добавлении компонента: " + ex.getMessage());
            }
        });
        return addButton;
    }
}