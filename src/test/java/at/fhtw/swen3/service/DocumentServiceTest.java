package at.fhtw.swen3.service;

import at.fhtw.swen3.dto.DocumentDto;
import at.fhtw.swen3.dto.DocumentRequest;
import at.fhtw.swen3.entity.Document;
import at.fhtw.swen3.exception.ResourceNotFoundException;
import at.fhtw.swen3.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        documentService = new DocumentService(documentRepository);
    }

    private Document sampleDocument(Long id) {
        Document document = new Document();
        document.setId(id);
        document.setTitle("Invoice");
        document.setFilename("invoice.pdf");
        document.setContentType("application/pdf");
        return document;
    }

    @Test
    void create_savesDocumentAndReturnsDto() {
        DocumentRequest request = new DocumentRequest();
        request.setTitle("Invoice");
        request.setFilename("invoice.pdf");
        request.setContentType("application/pdf");

        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> {
            Document toSave = invocation.getArgument(0);
            toSave.setId(1L);
            return toSave;
        });

        DocumentDto result = documentService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Invoice");
        assertThat(result.getUploadedAt()).isNotNull();
        verify(documentRepository, times(1)).save(any(Document.class));
        verifyNoMoreInteractions(documentRepository);
    }

    @Test
    void getById_returnsDto_whenDocumentExists() {
        when(documentRepository.findById(1L)).thenReturn(Optional.of(sampleDocument(1L)));

        DocumentDto result = documentService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Invoice");
    }

    @Test
    void getById_throws_whenDocumentMissing() {
        when(documentRepository.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.getById(42L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAll_mapsAllDocuments() {
        when(documentRepository.findAll()).thenReturn(List.of(sampleDocument(1L), sampleDocument(2L)));

        List<DocumentDto> result = documentService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(DocumentDto::getId).containsExactly(1L, 2L);
    }

    @Test
    void update_changesFieldsAndSaves() {
        Document existing = sampleDocument(1L);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DocumentRequest request = new DocumentRequest();
        request.setTitle("Updated title");
        request.setFilename("updated.pdf");
        request.setContentType("application/pdf");

        DocumentDto result = documentService.update(1L, request);

        assertThat(result.getTitle()).isEqualTo("Updated title");
        assertThat(result.getFilename()).isEqualTo("updated.pdf");
    }

    @Test
    void update_throws_whenDocumentMissing() {
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.update(99L, new DocumentRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_removesDocument_whenItExists() {
        Document existing = sampleDocument(1L);
        when(documentRepository.findById(1L)).thenReturn(Optional.of(existing));

        documentService.delete(1L);

        ArgumentCaptor<Document> captor = ArgumentCaptor.forClass(Document.class);
        verify(documentRepository).delete(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
    }

    @Test
    void delete_throws_whenDocumentMissing() {
        when(documentRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentService.delete(7L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(documentRepository, never()).delete(any());
    }
}
