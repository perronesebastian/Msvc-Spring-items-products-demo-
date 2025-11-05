package com.sebastian.springcloud.msvc.products.repositories;

import com.sebastian.springcloud.msvc.products.entities.Product;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, Long> {


}
