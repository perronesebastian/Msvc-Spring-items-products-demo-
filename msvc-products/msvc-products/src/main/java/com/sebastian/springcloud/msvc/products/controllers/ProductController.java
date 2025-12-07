package com.sebastian.springcloud.msvc.products.controllers;

import com.sebastian.springcloud.msvc.products.entities.Product;
import com.sebastian.springcloud.msvc.products.services.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
public class ProductController {

    final private ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Product>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> details(@PathVariable Long id) throws IllegalAccessException, InterruptedException {
        if (id.equals(10L)) {
            throw new IllegalAccessException("Product not found");
        }
        if (id.equals(7L)) {
            TimeUnit.SECONDS.sleep(2L);
        }
        return service.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
