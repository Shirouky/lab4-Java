package gui;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Objects;
import java.util.Vector;

public class WandView extends View implements ViewInterface {
    private JComboBox<String> woodComboBox, coreComboBox;
    private final JTable customerTable;

    public WandView(MainView mainFrame, CardLayout cardLayout, JPanel cardPanel, JTable customerTable) {
        super(mainFrame, cardLayout, cardPanel);

        columnNames.add("ID");
        columnNames.add("Древесина");
        columnNames.add("Сердцевина");
        columnNames.add("Длина");
        columnNames.add("Гибкость");
        columnNames.add("Цена");
        columnNames.add("Статус");
        columnNames.add("Покупатель");
        columnNames.add("Дата продажи");

        this.customerTable = customerTable;
    }

    @Override
    public void createPanel() {
        createPanel("Управление палочками", createButtonPanel(), "WandManagement");
        refreshTable();
    }

    @Override
    public JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton addWandButton = new JButton("Добавить палочку");
        addWandButton.addActionListener(e -> showAddWandDialog());
        buttonPanel.add(addWandButton);

        JButton sellWandButton = new JButton("Продать палочку");
        sellWandButton.addActionListener(e -> sellSelectedWand());
        buttonPanel.add(sellWandButton);

        JButton backButton = new JButton("Назад");
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "MainMenu"));
        buttonPanel.add(backButton);
        return buttonPanel;
    }

    @Override
    public void refreshTable() {
        try {
            Vector<Vector<Object>> data = controller.getAllWands();
            refreshTable(data, columnNames, table);
        } catch (SQLException e) {
            messageError(mainFrame, "Ошибка при загрузке данных: " + e.getMessage());
        }
    }

    public void showAddWandDialog() {
        JDialog dialog = new JDialog(mainFrame, "Добавление новой палочки", true);
        dialog.setSize(400, 400);

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));

        panel.add(new JLabel("Древесина:"));
        woodComboBox = new JComboBox<>();
        loadComponents(woodComboBox, "wood");
        panel.add(woodComboBox);

        panel.add(new JLabel("Сердцевина:"));
        coreComboBox = new JComboBox<>();
        loadComponents(coreComboBox, "core");
        panel.add(coreComboBox);

        panel.add(new JLabel("Длина (дюймы):"));
        JFormattedTextField lengthField = new JFormattedTextField(NumberFormat.getNumberInstance());
        panel.add(lengthField);

        panel.add(new JLabel("Гибкость:"));
        JTextField flexibilityField = new JTextField();
        panel.add(flexibilityField);

        panel.add(new JLabel("Цена:"));
        JFormattedTextField priceField = new JFormattedTextField(NumberFormat.getNumberInstance());
        panel.add(priceField);

        JButton addButton = addButton(dialog, lengthField, flexibilityField, priceField);

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public JButton addButton(JDialog dialog, JFormattedTextField lengthField, JTextField flexibilityField, JFormattedTextField priceField) {
        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(e -> {
            try {
                int woodId = Integer.parseInt(Objects.requireNonNull(woodComboBox.getSelectedItem()).toString().split(" - ")[0]);
                int coreId = Integer.parseInt(Objects.requireNonNull(coreComboBox.getSelectedItem()).toString().split(" - ")[0]);
                double length = Double.parseDouble(lengthField.getText());
                String flexibility = flexibilityField.getText();
                double price = Double.parseDouble(priceField.getText());

                controller.createWand(woodId, coreId, length, flexibility, price);
                messageSuccess(dialog, "Новая палочка успешно добавлена!");

                refreshTable();
                dialog.dispose();
            } catch (SQLException ex) {
                messageError(dialog, "Ошибка при добавлении палочки: " + ex.getMessage());
            } catch (NumberFormatException ex) {
                messageError(dialog, "Пожалуйста, введите корректное количество.");
            }
        });
        return addButton;
    }

    public void sellSelectedWand() {
        int wandId = super.selectRow(table);
        if (wandId == -1) return;

        JDialog dialog = new JDialog(mainFrame, "Продажа палочки", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel customerPanel = new JPanel(new BorderLayout());
        customerPanel.add(new JLabel("Выберите покупателя:"), BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(customerTable);
        customerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel customerButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        customerPanel.add(customerButtonPanel, BorderLayout.SOUTH);

        panel.add(customerPanel, BorderLayout.CENTER);

        JPanel buttonPanel = createCustomersButtonPanel(customerTable, dialog, wandId);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel createCustomersButtonPanel(JTable customerTable, JDialog dialog, int wandId) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton sellButton = new JButton("Продать");
        sellButton.addActionListener(e -> {
            int customerId = super.selectRow(customerTable);
            if (customerId == -1) return;

            dialog.dispose();
            sell(wandId, customerId);
        });

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(sellButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    public void sell(int wandId, int customerId) {
        try {
            controller.sellWand(wandId, customerId);
            messageSuccess(mainFrame, "Палочка успешно продана!");
            refreshTable();
        } catch (SQLException ex) {
            messageError(mainFrame, "Ошибка при продаже палочки: " + ex.getMessage());

        }
    }
}