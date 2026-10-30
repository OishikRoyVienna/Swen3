package at.fhtw.swen3.ocr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OcrWorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OcrWorkerApplication.class, args);
    }
}
