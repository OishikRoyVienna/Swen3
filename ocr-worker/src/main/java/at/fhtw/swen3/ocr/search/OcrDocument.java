package at.fhtw.swen3.ocr.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "documents")
public record OcrDocument(
        @Id String id,
        @Field(type = FieldType.Text) String filename,
        @Field(type = FieldType.Text) String content
) {
}
