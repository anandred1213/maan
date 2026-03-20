package com.order_service.order_microservice.consumer;

import com.order_service.order_microservice.dto.UserEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserEventConsumerTest {

    @InjectMocks
    private UserEventConsumer userEventConsumer;

    @Test
    void consume_Success() {
        UserEvent event = new UserEvent();
        event.setName("John Doe");
        event.setEmail("john@example.com");

        assertDoesNotThrow(() -> userEventConsumer.consume(event));
    }

    @Test
    void consume_NullEvent() {
        assertDoesNotThrow(() -> userEventConsumer.consume(null));
    }
}
