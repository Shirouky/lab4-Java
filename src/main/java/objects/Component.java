package objects;

public class Component {
    private int componentId;
    private String type; // "wood" или "core"
    private String name;
    private String description;
    private int quantity;

    // Конструкторы
    public Component() {
    }

    public Component(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getComponentId() {
        return componentId;
    }

    public void setComponentId(int componentId) {
        this.componentId = componentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
