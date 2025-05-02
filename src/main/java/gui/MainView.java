package gui;

import main.DatabaseConnector;

import javax.swing.*;
import java.awt.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class MainView extends JFrame{
    final String DB_URL = "jdbc:h2:./database/olivanders;" +
            "MODE=MySQL;" +  // Режим совместимости с MySQL
            "DATABASE_TO_UPPER=false";
    private final String DB_USER = "olivander";
    private final String DB_PASSWORD = "password";

    private JFrame mainFrame;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    CustomerView customerView = new CustomerView(this);
    SupplyView supplyView = new SupplyView(this);
    WandView wandView = new WandView(this);

    private void createMainWindow() {
        mainFrame = new JFrame("Магазин волшебных палочек Олливандеры");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(800, 600);
        mainFrame.setLocationRelativeTo(null);

        // Создаем панель с CardLayout для переключения между экранами
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Создаем экраны (панели) для разных функций
        createMainMenuPanel();
        wandView.createWandManagementPanel();
        customerView.createCustomerManagementPanel();
        supplyView.createSupplyManagementPanel();
        createInventoryPanel();

        // Добавляем панель карт в главное окно
        mainFrame.add(cardPanel);

        // Показываем главное меню при запуске
        cardLayout.show(cardPanel, "MainMenu");

        mainFrame.setVisible(true);
    }

    private void createMainMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Заголовок
        JLabel titleLabel = new JLabel("Магазин волшебных палочек Олливандеры", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Кнопки меню
        JPanel buttonPanel = new JPanel(new GridLayout(5, 1, 10, 10));

        JButton wandsButton = new JButton("Управление палочками");
        wandsButton.addActionListener(e -> cardLayout.show(cardPanel, "WandManagement"));
        buttonPanel.add(wandsButton);

        JButton customersButton = new JButton("Управление покупателями");
        customersButton.addActionListener(e -> cardLayout.show(cardPanel, "CustomerManagement"));
        buttonPanel.add(customersButton);

        JButton suppliesButton = new JButton("Управление поставками");
        suppliesButton.addActionListener(e -> cardLayout.show(cardPanel, "SupplyManagement"));
        buttonPanel.add(suppliesButton);

        JButton inventoryButton = new JButton("Просмотр состояния склада");
        inventoryButton.addActionListener(e -> {
            refreshInventoryTable();
            cardLayout.show(cardPanel, "Inventory");
        });
        buttonPanel.add(inventoryButton);

        JButton clearDataButton = new JButton("Очистить все данные");
        clearDataButton.addActionListener(e -> clearAllData());
        buttonPanel.add(clearDataButton);

        panel.add(buttonPanel, BorderLayout.CENTER);

        cardPanel.add(panel, "MainMenu");
    }

    private JTable inventoryTable;

    private void createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Заголовок
        JLabel titleLabel = new JLabel("Состояние склада", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Таблица инвентаря
        inventoryTable = new JTable();
        refreshInventoryTable();
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Кнопка "Назад"
        JButton backButton = new JButton("Назад");
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "MainMenu"));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(backButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        cardPanel.add(panel, "Inventory");
    }

    private void refreshInventoryTable() {
        try {
            String sql = "SELECT component_id, name, type, description, quantity " +
                    "FROM components ORDER BY type, name";

            Statement statement = DatabaseConnector.getConnection().createStatement();
            ResultSet result = statement.executeQuery(sql);

            // Создаем модель таблицы
            Vector<Vector<Object>> data = new Vector<>();
            Vector<String> columnNames = new Vector<>();

            // Заголовки столбцов
            columnNames.add("ID");
            columnNames.add("Название");
            columnNames.add("Тип");
            columnNames.add("Описание");
            columnNames.add("Количество");

            // Данные
            while (result.next()) {
                Vector<Object> row = new Vector<>();
                row.add(result.getInt("component_id"));
                row.add(result.getString("name"));
                row.add(result.getString("type").equals("wood") ? "Древесина" : "Сердцевина");
                row.add(result.getString("description") != null ? result.getString("description") : "-");
                row.add(result.getInt("quantity"));
                data.add(row);
            }

            inventoryTable.setModel(new javax.swing.table.DefaultTableModel(data, columnNames) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            });

            result.close();
            statement.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Ошибка при загрузке данных: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ========== Вспомогательные методы ==========
    private void clearAllData() {
        int confirm = JOptionPane.showConfirmDialog(mainFrame,
                "Вы уверены, что хотите полностью очистить все данные?\nЭто действие нельзя отменить.",
                "Подтверждение очистки", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Statement statement = DatabaseConnector.getConnection().createStatement();

                // Отключаем проверку внешних ключей для очистки таблиц
                statement.execute("SET FOREIGN_KEY_CHECKS = 0");

                // Очищаем таблицы в правильном порядке
                statement.execute("TRUNCATE TABLE wands");
                statement.execute("TRUNCATE TABLE customers");
                statement.execute("TRUNCATE TABLE supply_components");
                statement.execute("TRUNCATE TABLE supplies");
                statement.execute("TRUNCATE TABLE components");

                // Включаем проверку внешних ключей обратно
                statement.execute("SET FOREIGN_KEY_CHECKS = 1");

                statement.close();

                JOptionPane.showMessageDialog(mainFrame, "Все данные успешно очищены.",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);

                // Обновляем все таблицы
                wandView.refreshWandsTable();
//                customerView.refreshCustomersTable(customersTable);
                supplyView.refreshSupplyTable();
                refreshInventoryTable();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(mainFrame, "Ошибка при очистке данных: " + e.getMessage(),
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
