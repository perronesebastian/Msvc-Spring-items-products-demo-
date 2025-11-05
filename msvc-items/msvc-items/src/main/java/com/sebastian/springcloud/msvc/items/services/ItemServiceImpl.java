package com.sebastian.springcloud.msvc.items.services;

import com.sebastian.springcloud.msvc.items.clients.ProductFeignClient;
import com.sebastian.springcloud.msvc.items.models.Item;
import feign.FeignException;
import feign.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ItemServiceImpl implements ItemService {
    final private ProductFeignClient client;

    public ItemServiceImpl(ProductFeignClient client) {
        this.client = client;
    }

    @Override
    public List<Item> findAll() {
        var products = client.findAll();
        return products.stream().map(product -> new Item(product, new Random().nextInt(10) + 1)).toList();
    }

    @Override
    public Optional<Item> findById(Long id) {
        try {
            var product = client.findById(id);
            return Optional.of(new Item(product, new Random().nextInt(10) + 1));
        } catch (FeignException e) {
            return Optional.empty();
        }
    }
}
