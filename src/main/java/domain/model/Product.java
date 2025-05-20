package domain.model;

public class Product {
    private Long id;
    private String name;
    private String description;
    private double price;


    public Product() {}

    public Product(String name,String description, double price) {
        this.name = name;
        this.name = description;
        this.price = price;
    }

    public Product(Long id, String name, String description, double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;

    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }

}
