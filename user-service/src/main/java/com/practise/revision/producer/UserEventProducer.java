package com.practise.userservice.producer;

import com.practise.userservice.dto.UserEvent;
import io.micrometer.tracing.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Service
public class UserEventProducer {

  /*
  * Micrometer (with Spring Boot) automatically manages traceId and spanId in the main HTTP request thread, so logs printed during the handling of the HTTP request automatically have those IDs.
  * However, when you do asynchronous work (like sending messages via Kafka in a separate thread or using CompletableFuture.runAsync), the tracing context does not propagate automatically. That’s why your logs in the Kafka producer thread show empty traceId and spanId.
  *Using Tracer allows you to capture the current span from the main thread and propagate it manually to the async/Kafka thread. This ensures the trace information is carried over, and logs or spans created in Kafka-related threads will also have the same trace context.
  *
  * */

    private final Tracer tracer;
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(UserEventProducer.class);
    private static final String TOPIC = "order-topic";

    public UserEventProducer(Tracer tracer, KafkaTemplate<String, UserEvent> kafkaTemplate) {
        this.tracer = tracer;
        this.kafkaTemplate = kafkaTemplate;
    }





    public CompletableFuture<Void> sendUserCreatedEvent(UserEvent event) {
        var currentSpan = tracer.currentSpan();

        return CompletableFuture.runAsync(() -> {
            try (var scope = currentSpan != null ? tracer.withSpan(currentSpan) : null) {
                // Put trace info in MDC
                if (currentSpan != null) {
                    MDC.put("trace_id", currentSpan.context().traceId());
                    MDC.put("span_id", currentSpan.context().spanId());
                }

                kafkaTemplate.send(TOPIC, event).get();
                log.info("message sent successfully: {}", event);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Message sending interrupted: {}", event, e);
            } catch (ExecutionException e) {
                log.error("Message failed to send: {}", event, e);
            } finally {
                MDC.clear(); // clear after use
            }
        });
    }
}
