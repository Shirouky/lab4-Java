package main;

import gui.MainView;

public class Main {
    public static void main(String[] args) {
        DatabaseConnector dbConnector = new DatabaseConnector();
        dbConnector.createTables();
//        Controller controller = new Controller(dbConnector);
        MainView mainView = new MainView();
//        controller.setMainView(mainView);
        mainView.show();
    }
}