package at.fhtw.swen3.repository;

import at.fhtw.swen3.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
}
