package at.fhtw.swen3.service;

import at.fhtw.swen3.exception.RepositoryException;
import org.springframework.dao.DataAccessException;

import java.util.function.Supplier;

/**
 * Translates Spring's DataAccessException from repository calls into our persistence-layer RepositoryException.
 */
final class Persistence {

    private Persistence() {
    }

    static <T> T call(String action, Supplier<T> operation) {
        try {
            return operation.get();
        } catch (DataAccessException e) {
            throw new RepositoryException("Database error while trying to " + action, e);
        }
    }

    static void run(String action, Runnable operation) {
        call(action, () -> {
            operation.run();
            return null;
        });
    }
}
