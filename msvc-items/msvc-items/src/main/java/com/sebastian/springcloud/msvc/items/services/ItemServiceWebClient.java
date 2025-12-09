package com.sebastian.springcloud.msvc.items.services;

import com.sebastian.springcloud.libs.msvc.commons.entities.Product;
import com.sebastian.springcloud.msvc.items.models.Item;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;


@Service
public class ItemServiceWebClient implements ItemService {

    private final WebClient.Builder client;

    public ItemServiceWebClient(WebClient.Builder client) {
        this.client = client;
    }

    @Override
    public List<Item> findAll() {
        return this.client.build().get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Product.class)
                .map(product -> new Item(product, new Random().nextInt(10) + 1)).collectList()
                .block();
    }

    @Override
    public Optional<Item> findById(Long id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        //try {
            return Optional.ofNullable(client.build().get().uri("/{id}", params)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(Product.class)
                    .map(product -> new Item(product, new Random().nextInt(10) + 1))
                    .block());
        //} catch (WebClientResponseException e) {
        //    return Optional.empty();
        //}
    }

    @Override
    public Item create(Item item) {
        return client.build()
                .post()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(item.getProduct())
                .retrieve()
                .bodyToMono(Product.class)
                .map(p -> new Item(p, item.getQuantity()))
                .block();
    }

    @Override
    public Item update(Long id, Item item) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        return client.build()
                .put()
                .uri("/{id}", params)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(item.getProduct())
                .retrieve()
                .bodyToMono(Product.class)
                .map(p -> new Item(p, item.getQuantity()))
                .block();
    }

    @Override
    public void delete(Long id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        client.build()
                .delete()
                .uri("/{id}", params)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
