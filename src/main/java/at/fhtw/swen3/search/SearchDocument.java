package at.fhtw.swen3.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "documents", createIndex = false)
public class SearchDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String filename;

    @Field(type = FieldType.Text)
    private String content;

    public SearchDocument() {}

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
