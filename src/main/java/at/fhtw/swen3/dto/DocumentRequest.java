package at.fhtw.swen3.dto;

import jakarta.validation.constraints.NotBlank;

public class DocumentRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String filename;

    private String contentType;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
