package com.sebastian.springcloud.msvc.products.services;

import com.sebastian.springcloud.msvc.products.entities.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<Product> findAll();

    Optional<Product> findById(Long id);
}
