package com.paymentservice.payment.consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentConsumerTest {

    @InjectMocks
    private PaymentConsumer paymentConsumer;

    @Test
    void consumeOrder_Success() {
        String message = "{\"orderId\":1,\"userId\":1,\"amount\":100.0}";

        assertDoesNotThrow(() -> paymentConsumer.consumeOrder(message));
    }

    @Test
    void consumeOrder_EmptyMessage() {
        String message = "";

        assertDoesNotThrow(() -> paymentConsumer.consumeOrder(message));
    }

    @Test
    void consumeOrder_NullMessage() {
        assertDoesNotThrow(() -> paymentConsumer.consumeOrder(null));
    }
}
