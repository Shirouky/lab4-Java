package gui;

import main.Controller;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

public abstract class View extends JFrame {
    protected MainView mainFrame;
    protected CardLayout cardLayout;
    protected JPanel cardPanel;
    protected Controller controller;
    protected JTable table;
    protected final Vector<String> columnNames = new Vector<>();

    public View(MainView mainFrame, CardLayout cardLayout, JPanel cardPanel) {
        controller = new Controller();
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        this.mainFrame = mainFrame;
        table = new JTable();

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
    }

    protected void createPanel(String title, JPanel buttonPanel, String label) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel mainContentPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(table);
        mainContentPanel.add(scrollPane, BorderLayout.CENTER);
        mainContentPanel.add(buttonPanel, BorderLayout.SOUTH);

        panel.add(mainContentPanel, BorderLayout.CENTER);
        cardPanel.add(panel, label);
    }

    protected void refreshTable(Vector<Vector<Object>> data, Vector<String> columnNames, JTable table) {
        table.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
    }

    protected void popupTable(Vector<Vector<Object>> data, Vector<String> columnNames, String message, JDialog dialog) {
        if (data.isEmpty()) {
            messageInformation(dialog, message);
            dialog.dispose();
            return;
        }

        JTable newTable = new JTable(data, columnNames);
        refreshTable(data, columnNames, table);
        JScrollPane scrollPane = new JScrollPane(newTable);
        dialog.add(scrollPane);

        JButton closeButton = new JButton("Закрыть");
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);

        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    protected int selectRow(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (table.getSelectedRow() == -1) {
            messageError(mainFrame, "Пожалуйста, выберите строку таблицы.");
            return -1;
        }
        return (int) table.getValueAt(selectedRow, 0);
    }

    protected void messageError(Component dialog, String message) {
        JOptionPane.showMessageDialog(dialog, message, "Ошибка", JOptionPane.ERROR_MESSAGE);
    }

    protected void messageSuccess(Component dialog, String message) {
        JOptionPane.showMessageDialog(dialog, message, "Успех", JOptionPane.INFORMATION_MESSAGE);
    }

    protected void messageInformation(Component dialog, String message) {
        JOptionPane.showMessageDialog(dialog, message, "Информация", JOptionPane.INFORMATION_MESSAGE);
    }

    protected void loadComponents(JComboBox<String> comboBox, String type) {
        comboBox.removeAllItems();
        try {
            ArrayList<String> result = controller.getComponents(type);
            for (String res : result) {
                comboBox.addItem(res);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при загрузке компонентов: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}
