package com.sebastian.springcloud.msvc.products.services;

import com.sebastian.springcloud.libs.msvc.commons.entities.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<Product> findAll();

    Optional<Product> findById(Long id);

    Product create(Product product);

    Product update(Long id, Product product);

    void delete(Long id);
}
