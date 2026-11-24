package at.fhtw.swen3.messaging;

import at.fhtw.swen3.exception.ResourceNotFoundException;
import at.fhtw.swen3.service.DocumentService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SummaryListenerTest {

    private final DocumentService documentService = mock(DocumentService.class);
    private final SummaryListener listener = new SummaryListener(documentService);

    @Test
    void onSummary_savesSummaryViaService() {
        listener.onSummary(new SummaryMessage(5L, "Zusammenfassung"));

        verify(documentService).saveSummary(5L, "Zusammenfassung");
    }

    @Test
    void onSummary_discardsSummary_whenDocumentWasDeleted() {
        doThrow(new ResourceNotFoundException("Document not found: 6")).when(documentService).saveSummary(6L, "Text");

        assertThatCode(() -> listener.onSummary(new SummaryMessage(6L, "Text"))).doesNotThrowAnyException();
    }
}
