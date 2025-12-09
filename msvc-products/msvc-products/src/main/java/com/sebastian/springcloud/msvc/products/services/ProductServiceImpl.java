package com.sebastian.springcloud.msvc.products.services;

import com.sebastian.springcloud.libs.msvc.commons.entities.Product;
import com.sebastian.springcloud.msvc.products.repositories.ProductRepository;
import jakarta.ws.rs.NotFoundException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    final private ProductRepository productRepository;

    final private Environment environment;

    public ProductServiceImpl(ProductRepository repository, Environment environment) {
        this.productRepository = repository;
        this.environment = environment;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return ((List<Product>) productRepository.findAll()).stream().map(product -> {
            product.setPort(Integer.parseInt(environment.getProperty("local.server.port")));
            return product;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id).map(product -> {
            product.setPort(Integer.parseInt(environment.getProperty("local.server.port")));
            return product;
        });
    }

    @Override
    @Transactional
    public Product create(Product product) {
        product.setCreatedAt(LocalDate.now());
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product update(Long id, Product product) {
        var optionalProduct = productRepository.findById(id);
        if (optionalProduct.isEmpty()) {
            throw new NotFoundException("Product not found");
        }
        var result = optionalProduct.get();
        result.setName(product.getName());
        result.setPrice(product.getPrice());
        return productRepository.save(result);

    }

    @Override
    @Transactional
    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}
