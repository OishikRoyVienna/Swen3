package at.fhtw.swen3.repository;

import at.fhtw.swen3.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByDocumentId(Long documentId);
}
