package gui;

import database.Controller;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Objects;
import java.util.Vector;

public class WandView extends JFrame {
    private final MainView mainFrame;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final Controller controller;
    public JTable wandsTable;
    public JComboBox<String> woodComboBox, coreComboBox;

    public WandView(MainView mainFrame, CardLayout cardLayout, JPanel cardPanel) {
        controller = new Controller();
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        this.mainFrame = mainFrame;
        initializeUI();
    }

    public void initializeUI() {
        setTitle("Управление палочками");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public void refreshTable() {
        Vector<Vector<Object>> data;
        try {
            data = controller.getAllWands();
            Vector<String> columnNames = new Vector<>();

            columnNames.add("ID");
            columnNames.add("Древесина");
            columnNames.add("Сердцевина");
            columnNames.add("Длина");
            columnNames.add("Гибкость");
            columnNames.add("Цена");
            columnNames.add("Статус");
            columnNames.add("Покупатель");
            columnNames.add("Дата продажи");

            wandsTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
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

    public void createPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Управление палочками", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel mainContentPanel = new JPanel(new BorderLayout());

        wandsTable = new JTable();
        refreshTable();
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

        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));

        panel.add(new JLabel("Древесина:"));
        woodComboBox = new JComboBox<>();
        mainFrame.loadComponents(woodComboBox, "wood");
        panel.add(woodComboBox);

        panel.add(new JLabel("Сердцевина:"));
        coreComboBox = new JComboBox<>();
        mainFrame.loadComponents(coreComboBox, "core");
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

        JButton addButton = new JButton("Добавить");
        addButton.addActionListener(e -> {
            int woodId = Integer.parseInt(Objects.requireNonNull(woodComboBox.getSelectedItem()).toString().split(" - ")[0]);
            int coreId = Integer.parseInt(Objects.requireNonNull(coreComboBox.getSelectedItem()).toString().split(" - ")[0]);
            double length = Double.parseDouble(lengthField.getText());
            String flexibility = flexibilityField.getText();
            double price = Double.parseDouble(priceField.getText());
            try {
                controller.createWand(woodId, coreId, length, flexibility, price);
                controller.decreaseComponent(woodId, 1);
                controller.decreaseComponent(coreId, 1);
                JOptionPane.showMessageDialog(dialog, "Новая палочка успешно добавлена!",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);

                refreshTable();
                dialog.dispose();
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

    public void sellSelectedWand() {
        int selectedRow = wandsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainFrame, "Пожалуйста, выберите палочку для продажи.",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int wandId = (Integer) wandsTable.getValueAt(selectedRow, 0);
        String status = (String) wandsTable.getValueAt(selectedRow, 6);

        if (!status.equals("В наличии")) {
            JOptionPane.showMessageDialog(mainFrame, "Эта палочка уже продана!",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(mainFrame, "Продажа палочки", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(mainFrame);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel customerPanel = new JPanel(new BorderLayout());
        customerPanel.add(new JLabel("Выберите покупателя:"), BorderLayout.NORTH);

        JTable customersTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(customersTable);
        customerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel customerButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        customerPanel.add(customerButtonPanel, BorderLayout.SOUTH);

        panel.add(customerPanel, BorderLayout.CENTER);

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
            sale(wandId, customerId);
        });

        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(sellButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public void sale(int wandId, int customerId) {
        try {
            controller.saleWand(wandId, customerId);
            JOptionPane.showMessageDialog(mainFrame, "Палочка успешно продана!",
                    "Успех", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при продаже палочки: " + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);

        }
    }
}