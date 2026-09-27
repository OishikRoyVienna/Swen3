package at.fhtw.swen3.controller;

import at.fhtw.swen3.dto.DocumentDto;
import at.fhtw.swen3.dto.DocumentRequest;
import at.fhtw.swen3.exception.ResourceNotFoundException;
import at.fhtw.swen3.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DocumentService documentService;

    private DocumentDto sampleDto(Long id) {
        DocumentDto dto = new DocumentDto();
        dto.setId(id);
        dto.setTitle("Invoice");
        dto.setFilename("invoice.pdf");
        dto.setContentType("application/pdf");
        dto.setUploadedAt(LocalDateTime.now());
        dto.setComments(List.of());
        return dto;
    }

    @Test
    void create_returns201() throws Exception {
        when(documentService.create(any())).thenReturn(sampleDto(1L));

        DocumentRequest request = new DocumentRequest();
        request.setTitle("Invoice");
        request.setFilename("invoice.pdf");
        request.setContentType("application/pdf");

        mockMvc.perform(post("/api/documents")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Invoice"));
    }

    @Test
    void create_returns400_whenTitleMissing() throws Exception {
        DocumentRequest request = new DocumentRequest();
        request.setFilename("invoice.pdf");

        mockMvc.perform(post("/api/documents")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_returns200() throws Exception {
        when(documentService.getById(1L)).thenReturn(sampleDto(1L));

        mockMvc.perform(get("/api/documents/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filename").value("invoice.pdf"));
    }

    @Test
    void getById_returns404_whenMissing() throws Exception {
        when(documentService.getById(99L)).thenThrow(new ResourceNotFoundException("Document not found: 99"));

        mockMvc.perform(get("/api/documents/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_returnsList() throws Exception {
        when(documentService.getAll()).thenReturn(List.of(sampleDto(1L), sampleDto(2L)));

        mockMvc.perform(get("/api/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/documents/1"))
                .andExpect(status().isNoContent());
    }
}
