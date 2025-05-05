package main;

import database.DatabaseConnector;
import gui.MainView;

public class Main {
    public static void main(String[] args) {
        DatabaseConnector dbConnector = new DatabaseConnector();
        dbConnector.createTables();
        new MainView();
    }
}