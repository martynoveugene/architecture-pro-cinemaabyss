package com.cinemaabyss.controller;

import com.cinemaabyss.model.*;
import com.cinemaabyss.model.Error;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@Slf4j
@RequestMapping("/api/events")
public class EventsController {

    @Autowired
    private KafkaTemplate<String, Identifiable> kafkaTemplate;

    @Autowired
    private Validator validator;

    @GetMapping(value = "/health", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> health(){
        return ResponseEntity.ok(Map.of("status", true));
    }

    @PostMapping(value = "/movie", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> postMovieEvent(@RequestBody MovieEvent movieEvent){
        return postEvent(
                "movie-events",
                movieEvent,
                Event.builder()
                        .id(movieEvent.getId())
                        .type("movie")
                        .timestamp(new Date())
                        .build()
        );
    }

    @PostMapping(value = "/user", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> postUserEvent(@RequestBody UserEvent userEvent) {
        return postEvent(
                "user-events",
                userEvent,
                Event.builder()
                        .id(userEvent.getId())
                        .type("user")
                        .timestamp(new Date())
                        .build()
        );
    }

    @PostMapping(value = "/payment", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> postPaymentEvent(@RequestBody PaymentEvent paymentEvent) {
        return postEvent(
                "payment-events",
                paymentEvent,
                Event.builder()
                        .id(paymentEvent.getId())
                        .type("payment")
                        .timestamp(new Date())
                        .build()
        );
    }

    private ResponseEntity<Object> postEvent(String topic, Identifiable requestEvent, Event responseEvent) {
        log.info(">>> Event '{}' - received to post = '{}'", requestEvent.getId(), requestEvent);

        var violations = validator.validate(requestEvent);

        if( ! violations.isEmpty() ){
            String errors = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));

            return ResponseEntity.badRequest().body(new Error(errors));
        }

        try {
            var result = kafkaTemplate.send(topic, requestEvent).get(3, TimeUnit.SECONDS);

            EventResponse response = EventResponse.builder()
                    .offset(result.getRecordMetadata().offset())
                    .partition(result.getRecordMetadata().partition())
                    .status("success")
                    .event(responseEvent)
                    .build()
                    ;

            log.info(">>> Event '{}' - sent successfully", requestEvent.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            log.error(">>> Event '{}' - failed to send", requestEvent.getId());
            return ResponseEntity.internalServerError().body(new Error(ex.getMessage()));
        }
    }
}
