package database;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Vector;

public class Controller {
    private final WandDB wandController;
    private final ComponentDB componentController;
    private final CustomerDB customerController;
    private final SupplyDB supplyController;

    public Controller() {
        wandController = new WandDB();
        componentController = new ComponentDB();
        customerController = new CustomerDB();
        supplyController = new SupplyDB();
    }

    public Vector<Vector<Object>> getAllWands() throws SQLException {
        return wandController.getAll();
    }

    public void saleWand(int wandId, int customerId) throws SQLException {
        wandController.sale(wandId, customerId);
    }

    public void createWand(int woodId, int coreId, double length, String flexibility, double price) throws SQLException {
        wandController.create(woodId, coreId, length, flexibility, price);
    }

    public ArrayList<String> getComponents(String type) throws SQLException {
        return componentController.get(type);
    }

    public void decreaseComponent(int componentId, int quantity) throws SQLException {
        componentController.decrease(componentId, quantity);
    }

    public void increaseComponents(int componentId, int quantity) throws SQLException {
        componentController.increase(componentId, quantity);
    }

    public Vector<Vector<Object>> getAllCustomers() throws SQLException {
        return customerController.getAll();
    }

    public Vector<Vector<Object>> getWand(int customerId) throws SQLException {
        return wandController.get(customerId);
    }

    public void createCustomer(String firstName, String lastName, String magicSchool) throws SQLException {
        customerController.create(firstName, lastName, magicSchool);
    }

    public void createSupply(String date, String supplier) throws SQLException, ParseException {
        supplyController.create(date, supplier);
    }

    public Vector<Vector<Object>> getAllSupplies() throws SQLException {
        return supplyController.getAll();
    }

    public void createComponents(int supplyId, int componentId, int quantity) throws SQLException {
        componentController.create(supplyId, componentId, quantity);
    }

    public Vector<Vector<Object>> getAllComponents() throws SQLException {
        return componentController.getAll();
    }

    public Vector<Vector<Object>> getComponentsSupply(int supplyId) throws SQLException {
        return componentController.getComponentsSupply(supplyId);
    }
    
    public void clearAll() throws SQLException {
        DatabaseConnector.clearAll();
    }
}
