package at.fhtw.swen3.ocr.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class DeadLetterQueueMonitorTest {

    private static final String DLQ = "paperless.ocr.dlq";

    @Mock
    private AmqpAdmin amqpAdmin;

    @Test
    void checkDeadLetterQueue_warns_whenFailedJobsAreWaiting(CapturedOutput output) {
        when(amqpAdmin.getQueueInfo(DLQ)).thenReturn(new QueueInformation(DLQ, 2, 0));

        new DeadLetterQueueMonitor(amqpAdmin, DLQ).checkDeadLetterQueue();

        assertThat(output).contains("2 failed OCR job(s) waiting");
    }

    @Test
    void checkDeadLetterQueue_doesNotWarn_whenQueueIsEmpty(CapturedOutput output) {
        when(amqpAdmin.getQueueInfo(DLQ)).thenReturn(new QueueInformation(DLQ, 0, 0));

        new DeadLetterQueueMonitor(amqpAdmin, DLQ).checkDeadLetterQueue();

        assertThat(output).doesNotContain("failed OCR job(s) waiting");
    }
}
