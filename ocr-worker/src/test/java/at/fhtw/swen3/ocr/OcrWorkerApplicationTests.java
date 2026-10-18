package at.fhtw.swen3.ocr;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// The listener is not started, so no RabbitMQ broker is needed for this test
@SpringBootTest(properties = "spring.rabbitmq.listener.simple.auto-startup=false")
class OcrWorkerApplicationTests {

    @Test
    void contextLoads() {
    }
}
