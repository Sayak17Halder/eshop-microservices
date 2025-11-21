package com.eshop.product_service.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Document(collection = "products")
@Getter
@Setter
public class Product {
    @Id
    private String id;

    @NotBlank
    private String name;

    private String description;

    private BigDecimal price;

    private int stock;

    // constructors, getters, setters
    public Product() {}
    public Product(String name, String description, BigDecimal price, int stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }
    // getters and setters ...
    // (Generate in IntelliJ or paste manually)
}
