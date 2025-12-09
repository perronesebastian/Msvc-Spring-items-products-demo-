package com.sebastian.springcloud.msvc.items.controllers;

import com.sebastian.springcloud.libs.msvc.commons.entities.Product;
import com.sebastian.springcloud.msvc.items.models.Item;
import com.sebastian.springcloud.msvc.items.services.ItemService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RefreshScope
@RestController
public class ItemController {

    private static final Logger log = LoggerFactory.getLogger(ItemController.class);
    private final ItemService itemService;
    private final CircuitBreakerFactory circuitBreakerFactory;

    @Autowired
    private Environment environment;

    @Value("${configuration.text}")
    private String text;

    public ItemController(@Qualifier("itemServiceWebClient") ItemService itemService, CircuitBreakerFactory circuitBreakerFactory) {
        this.itemService = itemService;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @GetMapping("/fetch-configs")
    public ResponseEntity<?> fetchConfigs(@Value("${server.port}") String port) {
        Map<String, String> json = new HashMap<>();
        json.put("text", text);
        json.put("port", port);

        if (environment.getActiveProfiles().length > 0 && environment.getActiveProfiles()[0].equals("dev")) {
            json.put("author.name", environment.getProperty("configuration.author.name"));
            json.put("author.email", environment.getProperty("configuration.author.email"));
        }
        return ResponseEntity.ok(json);
    }

    @GetMapping
    public ResponseEntity<List<Item>> list(@RequestParam(value = "name", required = false) String name,
                                           @RequestHeader(value = "token-request", required = false) String token) {
        System.out.println(name);
        System.out.println(token);
        return ResponseEntity.ok(itemService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable Long id) {
        Optional<Item> item = circuitBreakerFactory.create("items")
                .run(() -> itemService.findById(id), e -> {
                    log.error(e.getMessage());
                    Product product = new Product();
                    product.setCreatedAt(LocalDate.now());
                    product.setId(1L);
                    product.setName("null");
                    product.setName("product default");
                    product.setPrice(1D);
                    product.setPort(1);
                    return Optional.of(new Item(product, 1));
                });
        if (item.isPresent()) {
            return ResponseEntity.ok(item.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("message", "Product not found"));
    }

    @CircuitBreaker(name = "items", fallbackMethod = "getFallbackMethodProductV2")
    @GetMapping("/v2/{id}")
    public ResponseEntity<?> detailsV2(@PathVariable Long id) {
        Optional<Item> item =  itemService.findById(id);
        if (item.isPresent()) {
            return ResponseEntity.ok(item.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("message", "Product not found"));
    }

    @CircuitBreaker(name = "items", fallbackMethod = "getFallbackMethodProductV3")
    @TimeLimiter(name = "items")
    @GetMapping("/v3/{id}")
    public CompletableFuture<?> detailsV3(@PathVariable Long id) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Item> item =  itemService.findById(id);
            if (item.isPresent()) {
                return ResponseEntity.ok(item.get());
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("message", "Product not found"));
        });
    }

    @PostMapping
    public ResponseEntity<Item> create(@RequestBody Item item) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.create(item));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> update(@PathVariable Long id, @RequestBody Item item) {
        return ResponseEntity.ok().body(itemService.update(id, item));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.delete(id);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<?> getFallbackMethodProductV2(Throwable e) {
        log.error(e.getMessage());
        Product product = new Product();
        product.setCreatedAt(LocalDate.now());
        product.setId(1L);
        product.setName("null");
        product.setName("product default");
        product.setPrice(1D);
        product.setPort(1);
        return ResponseEntity.ok(new Item(product, 1));
    }

    public CompletableFuture<?> getFallbackMethodProductV3(Throwable e) {
        return CompletableFuture.supplyAsync(() -> {
            log.error(e.getMessage());
            Product product = new Product();
            product.setCreatedAt(LocalDate.now());
            product.setId(1L);
            product.setName("null");
            product.setName("product default");
            product.setPrice(1D);
            product.setPort(1);
            return ResponseEntity.ok(new Item(product, 1));
        });
    }
}
