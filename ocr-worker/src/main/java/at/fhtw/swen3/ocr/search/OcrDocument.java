package at.fhtw.swen3.ocr.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "documents")
public class OcrDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String filename;

    @Field(type = FieldType.Text)
    private String content;

    public OcrDocument() {
    }

    public OcrDocument(String id, String filename, String content) {
        this.id = id;
        this.filename = filename;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getFilename() {
        return filename;
    }

    public String getContent() {
        return content;
    }
}