package com.sebastian.springcloud.msvc.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.util.Optional;

@Component
public class SampleGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(SampleGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Executing filter");

        ServerHttpRequest newRequest = exchange.getRequest().mutate().headers(h -> h.add("token", "testToken")).build();

        ServerWebExchange newExchange = exchange.mutate().request(newRequest).build();

        return chain.filter(newExchange).then(Mono.fromRunnable(() -> {
            log.info("Executing POST filter response");
            String token = newExchange.getRequest().getHeaders().getFirst("token");
            log.info("token: " + token);

            Optional.ofNullable(token).ifPresent(value -> {
                newExchange.getResponse().getHeaders().add("token", value);
            });
            newExchange.getResponse().getCookies().add("color", ResponseCookie.from("color", "value").build());
            //newExchange.getResponse().getHeaders().setContentType(MediaType.TEXT_PLAIN);
        }));
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
