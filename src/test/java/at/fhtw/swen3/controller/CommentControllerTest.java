package at.fhtw.swen3.controller;

import at.fhtw.swen3.dto.CommentDto;
import at.fhtw.swen3.dto.CommentRequest;
import at.fhtw.swen3.exception.ResourceNotFoundException;
import at.fhtw.swen3.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    private CommentDto sampleDto(Long id, Long documentId) {
        CommentDto dto = new CommentDto();
        dto.setId(id);
        dto.setText("Looks good");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setDocumentId(documentId);
        return dto;
    }

    @Test
    void addComment_returns201() throws Exception {
        when(commentService.addComment(eq(1L), any())).thenReturn(sampleDto(10L, 1L));

        CommentRequest request = new CommentRequest();
        request.setText("Looks good");

        mockMvc.perform(post("/api/documents/1/comments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.documentId").value(1));
    }

    @Test
    void addComment_returns404_whenDocumentMissing() throws Exception {
        when(commentService.addComment(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("Document not found: 99"));

        CommentRequest request = new CommentRequest();
        request.setText("Looks good");

        mockMvc.perform(post("/api/documents/99/comments")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getComments_returnsList() throws Exception {
        when(commentService.getComments(1L)).thenReturn(List.of(sampleDto(10L, 1L), sampleDto(11L, 1L)));

        mockMvc.perform(get("/api/documents/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deleteComment_returns204() throws Exception {
        doNothing().when(commentService).deleteComment(1L, 10L);

        mockMvc.perform(delete("/api/documents/1/comments/10"))
                .andExpect(status().isNoContent());
    }
}
