package main;

import database.*;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Vector;

public class Controller {
    private final WandDB wandController = new WandDB();
    private final ComponentDB componentController = new ComponentDB();
    private final CustomerDB customerController = new CustomerDB();
    private final SupplyDB supplyController = new SupplyDB();

    public Vector<Vector<Object>> getAllWands() throws SQLException {
        return wandController.getAll();
    }

    public Vector<Vector<Object>> getWand(int customerId) throws SQLException {
        return wandController.get(customerId);
    }

    public void sellWand(int wandId, int customerId) throws SQLException {
        wandController.sell(wandId, customerId);
    }

    public void createWand(int woodId, int coreId, double length, String flexibility, double price) throws SQLException {
        wandController.create(woodId, coreId, length, flexibility, price);
    }

    public Vector<Vector<Object>> getAllCustomers() throws SQLException {
        return customerController.getAll();
    }

    public void createCustomer(String firstName, String lastName, String magicSchool) throws SQLException {
        customerController.create(firstName, lastName, magicSchool);
    }


    public Vector<Vector<Object>> getAllSupplies() throws SQLException {
        return supplyController.getAll();
    }

    public void createSupply(String date, String supplier) throws SQLException, ParseException {
        supplyController.create(date, supplier);
    }

    public Vector<Vector<Object>> getAllComponents() throws SQLException {
        return componentController.getAll();
    }

    public ArrayList<String> getComponents(String type) throws SQLException {
        return componentController.get(type);
    }

    public Vector<Vector<Object>> getComponentsSupply(int supplyId) throws SQLException {
        return componentController.getComponentsSupply(supplyId);
    }

    public void createComponents(int supplyId, int componentId, int quantity) throws SQLException {
        componentController.create(supplyId, componentId, quantity);
    }

    public void clearAll() throws SQLException {
        DatabaseConnector.clearAll();
    }
}
