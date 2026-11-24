package at.fhtw.swen3.genai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Dummy-Key und Listener aus: der Test startet nur den Spring-Kontext, ohne Gemini und ohne RabbitMQ
@SpringBootTest(properties = {
        "spring.ai.google.genai.api-key=test-key",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
class GenAiWorkerApplicationTests {

    @Test
    void contextLoads() {
    }
}
