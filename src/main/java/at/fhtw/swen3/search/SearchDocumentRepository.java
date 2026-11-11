package at.fhtw.swen3.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface SearchDocumentRepository
        extends ElasticsearchRepository<SearchDocument, String> {

    List<SearchDocument> findByContentContainingIgnoreCaseOrFilenameContainingIgnoreCase(
            String content, String filename);
}
