package gui;

import database.Controller;

import javax.swing.*;
import java.awt.*;

public abstract class View extends JFrame{
    protected MainView mainFrame;
    protected CardLayout cardLayout;
    protected JPanel cardPanel;
    protected Controller controller;

    public View(MainView mainFrame, CardLayout cardLayout, JPanel cardPanel) {
        controller = new Controller();
        this.cardPanel = cardPanel;
        this.cardLayout = cardLayout;
        this.mainFrame = mainFrame;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
    }

    public JPanel createPanel(String title){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));
        panel.add(titleLabel, BorderLayout.NORTH);
        return panel;
    }
}
