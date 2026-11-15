package at.fhtw.swen3.ocr.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface OcrDocumentRepository
        extends ElasticsearchRepository<OcrDocument, String> {
}