package com.cinemaabyss.service;

import com.cinemaabyss.model.MovieEvent;
import com.cinemaabyss.model.PaymentEvent;
import com.cinemaabyss.model.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KafkaConsumerService {

    @KafkaListener(topics = "movie-events")
    public void consumeMovieEvent(MovieEvent event) {
        log.info("<<< Event '{}' - consumed; content is '{}'", event.getId(), event);
    }

    @KafkaListener(topics = "user-events")
    public void consumeUserEvent(UserEvent event) {
        log.info("<<< Event '{}' - consumed; content is '{}'", event.getId(), event);
    }

    @KafkaListener(topics = "payment-events")
    public void consumePaymentEvent(PaymentEvent event) {
        log.info("<<< Event '{}' - consumed; content is '{}'", event.getId(), event);
    }
}
