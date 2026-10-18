package at.fhtw.swen3.messaging;

import at.fhtw.swen3.exception.MessageQueueException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OcrMessageProducerTest {

    private static final String QUEUE = "paperless.ocr";

    @Mock
    private RabbitTemplate rabbitTemplate;

    private OcrMessageProducer producer;

    private final OcrMessage message = new OcrMessage(1L, "invoice.pdf", "application/pdf", LocalDateTime.now());

    @BeforeEach
    void setUp() {
        producer = new OcrMessageProducer(rabbitTemplate, QUEUE);
    }

    @Test
    void send_publishesMessageToOcrQueue() {
        producer.send(message);

        verify(rabbitTemplate).convertAndSend(QUEUE, message);
    }

    @Test
    void send_wrapsAmqpException_inMessageQueueException() {
        doThrow(new AmqpConnectException(new RuntimeException("connection refused")))
                .when(rabbitTemplate).convertAndSend(eq(QUEUE), any(Object.class));

        assertThatThrownBy(() -> producer.send(message))
                .isInstanceOf(MessageQueueException.class)
                .hasMessageContaining("document 1");
    }
}
