package gui;

import database.Controller;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Vector;

public class ComponentView extends JFrame{
    private MainView mainFrame;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final Controller controller;
    private JTable inventoryTable;

    public ComponentView(MainView mainFrame, CardLayout cardLayout, JPanel cardPanel) {
        this.mainFrame = mainFrame;
        setTitle("Управление компонентами");
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

        JLabel titleLabel = new JLabel("Состояние склада", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        inventoryTable = new JTable();
        refreshTable();
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton backButton = new JButton("Назад");
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "MainMenu"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(backButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        cardPanel.add(panel, "Inventory");
    }

    public void refreshTable() {
        try {
            Vector<String> columnNames = new Vector<>();
            columnNames.add("ID");
            columnNames.add("Название");
            columnNames.add("Тип");
            columnNames.add("Количество");

            Vector<Vector<Object>> data = controller.getAllComponents();
            inventoryTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
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
}
