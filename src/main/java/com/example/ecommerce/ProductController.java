package com.example.ecommerce;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {
    private final List<Product> products = List.of(
        new Product(1, "Wireless Mouse", 599.00, "Accessories"),
        new Product(2, "USB-C Hub", 1299.00, "Accessories"),
        new Product(3, "Bluetooth Headphones", 1999.00, "Audio")
    );

    @GetMapping("/")
    public String home() {
        return "E-Commerce Store API is running. Visit /api/products";
    }

    @GetMapping("/api/products")
    public List<Product> products() {
        return products;
    }
}
