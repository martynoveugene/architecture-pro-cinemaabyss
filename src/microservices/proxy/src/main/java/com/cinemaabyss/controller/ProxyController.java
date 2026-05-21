package com.cinemaabyss.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@Slf4j
@RequestMapping("")
public class ProxyController {

    @Value("${MONOLITH_URL:}")
    private String MONOLITH_URL;

    @Value("${MOVIES_SERVICE_URL:}")
    private String MOVIES_SERVICE_URL;

    @Value("${EVENTS_SERVICE_URL:}")
    private String EVENTS_SERVICE_URL;

    @Value("${GRADUAL_MIGRATION:}")
    private String GRADUAL_MIGRATION;

    @Value("${MOVIES_MIGRATION_PERCENT:0}")
    private Integer MOVIES_MIGRATION_PERCENT;

    @Autowired
    private WebClient webClient;

    @GetMapping(value = "/health", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> health(){
        return ResponseEntity.ok(Map.of("status", true));
    }


    @RequestMapping("/**")
    public Mono<ResponseEntity<byte[]>> proxy(HttpServletRequest request){
        log.info(">>> received = '{}'", request.getRequestURI());

        int randomValue = ThreadLocalRandom.current().nextInt(1, 101);
        String uri = request.getRequestURI();
        String[] parts = uri.split("/");

        String pathFirstPart = (parts.length > 2) ? parts[2] : "";

        String targetUrl = switch (pathFirstPart) {
            case "movies" -> MOVIES_SERVICE_URL;
            case "events" -> EVENTS_SERVICE_URL;
            default -> MONOLITH_URL;
        };
        // При включенном фиче-флаге маршрутизирует определенный процент трафика в микросервис
        // При отключенном маршрутизирует весь трафик для определенного домена в соответствующий микросервис
        if( "true".equalsIgnoreCase(GRADUAL_MIGRATION) && randomValue > MOVIES_MIGRATION_PERCENT ){
            targetUrl = MONOLITH_URL;
            log.info(">>> processing via monolith = '{}'", targetUrl);
        } else {
            log.info(">>> processing via service = '{}'", targetUrl);
        }

        targetUrl = targetUrl + request.getRequestURI();
        final String finalTargetUrl = targetUrl;

        return webClient
                .method(HttpMethod.valueOf(request.getMethod()))
                .uri(uriBuilder -> {
                    return UriComponentsBuilder.fromHttpUrl(finalTargetUrl)
                            .query(request.getQueryString())
                            .build()
                            .toUri();
                })
                .headers(httpHeaders -> Collections.list(request.getHeaderNames())
                        .forEach(name -> {
                            String value = request.getHeader(name);
                            if( ! "Content-Length".equalsIgnoreCase(name) && ! "Host".equalsIgnoreCase(name) ) {
                                httpHeaders.add(name, value);
                            }
                        }))
                .body(BodyInserters.fromPublisher(DataBufferUtils.readInputStream(
                        request::getInputStream, new DefaultDataBufferFactory(), 4096), DataBuffer.class))
                .exchangeToMono(response ->
                        response.toEntity(byte[].class)
                )
                .onErrorReturn(ResponseEntity.status(504).body("Service Timeout or Unreachable".getBytes()));
    }
}
