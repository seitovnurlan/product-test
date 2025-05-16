package domain.model;

public class Product {
    private String name;
    private double price;
    private Integer id;

    public Product(String name, double price, Integer id) {
        this.name = name;
        this.price = price;
        this.id = id;
    }

    // Геттеры и сеттеры
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getId() { return id; }

    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setId(int id) { this.id = id; }
}
