package gui;

import database.Controller;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

public class SupplyView extends View{
    private JTable supplyTable;

    public SupplyView(MainView mainFrame, CardLayout cardLayout, JPanel cardPanel) {
        super(mainFrame, cardLayout, cardPanel);
        setTitle("Управление поставками");
    }

    public void createPanel() {
        String title = "Управление поставками";

        JPanel panel = super.createPanel(title);
        JPanel mainContentPanel = new JPanel(new BorderLayout());

        supplyTable = new JTable();
        refreshTable();
        JScrollPane scrollPane = new JScrollPane(supplyTable);
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);

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

        mainContentPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(mainContentPanel, BorderLayout.CENTER);

        cardPanel.add(panel, "SupplyManagement");
    }

    public void refreshTable() {
        try {
            Vector<String> columnNames = new Vector<>();
            columnNames.add("ID");
            columnNames.add("Дата");
            columnNames.add("Поставщик");

            Vector<Vector<Object>> data = controller.getAllSupplies();
            supplyTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
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

        JButton addButton = getjButton(dateField, supplierField, dialog, controller);

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JButton getjButton(JTextField dateField, JTextField supplierField, JDialog dialog, Controller controller) {
        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(e -> {
            String dateStr = dateField.getText();
            String supplier = supplierField.getText();
            try {
                controller.createSupply(dateStr, supplier);
                JOptionPane.showMessageDialog(dialog, "Новая поставка успешно добавлена",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                dialog.dispose();

            } catch (IllegalArgumentException | ParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Пожалуйста, введите дату в формате ГГГГ-ММ-ДД.",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Ошибка при добавлении поставки: " + ex.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
        return addButton;
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
            Vector<String> columnNames = new Vector<>();

            columnNames.add("ID");
            columnNames.add("Название");
            columnNames.add("Тип");
            columnNames.add("Количество");

            Vector<Vector<Object>> data = controller.getComponentsSupply(supplyId);
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
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при загрузке данных: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showAddComponentDialog() {
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
            mainFrame.loadComponents(componentComboBox, type);
        });

        // Инициализация списка компонентов
        mainFrame.loadComponents(componentComboBox, "wood");

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

                controller.createComponents(supplyId, componentId, quantity);
                controller.increaseComponents(componentId, quantity);

                JOptionPane.showMessageDialog(dialog, "Компонент успешно добавлен в поставку!",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();

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
}