package domain.model;

public class Product {
    private String name;
    private double price;
    private Long id;

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public Product(String name, double price, Long id) {
        this.name = name;
        this.price = price;
        this.id = id;
    }

    // Геттеры и сеттеры
    public String getName() { return name; }
    public double getPrice() { return price; }
    public Long getId() { return id; }

    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    public void setId(Long id) { this.id = id; }
}
