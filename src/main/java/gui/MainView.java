package gui;

import database.Controller;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class MainView extends JFrame {
    private final JFrame mainFrame;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    CustomerView customerView;
    SupplyView supplyView;
    WandView wandView;
    ComponentView componentView;
    private final Controller controller;

    public MainView() {
        mainFrame = new JFrame("Магазин волшебных палочек Олливандеры");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);
        mainFrame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        createPanel();
        wandView = new WandView(this, cardLayout, cardPanel);
        customerView = new CustomerView(this, cardLayout, cardPanel);
        supplyView = new SupplyView(this, cardLayout, cardPanel);
        componentView = new ComponentView(this, cardLayout, cardPanel);

        wandView.createPanel();
        customerView.createPanel();
        supplyView.createPanel();
        componentView.createPanel();

        controller = new Controller();

        mainFrame.add(cardPanel);
        cardLayout.show(cardPanel, "MainMenu");

        mainFrame.setVisible(true);
    }

    private void createPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Магазин волшебных палочек Олливандеры", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));

        JButton wandsButton = new JButton("Управление палочками");
        wandsButton.addActionListener(e -> {
            wandView.refreshTable();
            cardLayout.show(cardPanel, "WandManagement");
        });
        buttonPanel.add(wandsButton);

        JButton customersButton = new JButton("Управление покупателями");
        customersButton.addActionListener(e -> {
            customerView.refreshTable();
            cardLayout.show(cardPanel, "CustomerManagement");
        });
        buttonPanel.add(customersButton);

        JButton suppliesButton = new JButton("Управление поставками");
        suppliesButton.addActionListener(e -> {
            supplyView.refreshTable();
            cardLayout.show(cardPanel, "SupplyManagement");
        });
        buttonPanel.add(suppliesButton);

        JButton inventoryButton = new JButton("Просмотр состояния склада");
        inventoryButton.addActionListener(e -> {
            componentView.refreshTable();
            cardLayout.show(cardPanel, "Inventory");
        });
        buttonPanel.add(inventoryButton);

        JButton clearDataButton = new JButton("Очистить все данные");
        clearDataButton.addActionListener(e -> clearAll());
        buttonPanel.add(clearDataButton);

        panel.add(buttonPanel, BorderLayout.CENTER);

        cardPanel.add(panel, "MainMenu");
    }


    private void clearAll() {
        try {
            controller.clearAll();
            JOptionPane.showMessageDialog(mainFrame, "Все данные успешно очищены.",
                    "Успех", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при очистке данных: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadComponents(JComboBox<String> comboBox, String type) {
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
