package gui;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Vector;

public class ComponentView extends View implements ViewInterface{
    public ComponentView(MainView mainFrame, CardLayout cardLayout, JPanel cardPanel) {
        super(mainFrame, cardLayout, cardPanel);

        columnNames.add("ID");
        columnNames.add("Название");
        columnNames.add("Тип");
        columnNames.add("Количество");
    }

    @Override
    public void createPanel() {
        createPanel("Состояние склада", createButtonPanel(), "Inventory");
        refreshTable();
    }

    @Override
    public JPanel createButtonPanel() {
        JButton backButton = new JButton("Назад");
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "MainMenu"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(backButton);
        return buttonPanel;
    }

    @Override
    public void refreshTable() {
        try {
            Vector<Vector<Object>> data = controller.getAllComponents();
            refreshTable(data, columnNames, table);
        } catch (SQLException e) {
            messageError(mainFrame, "Ошибка при загрузке данных: " + e.getMessage());
        }
    }
}
