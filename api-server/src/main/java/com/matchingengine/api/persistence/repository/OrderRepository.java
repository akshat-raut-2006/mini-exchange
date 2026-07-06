package com.matchingengine.api.persistence.repository;

import com.matchingengine.api.persistence.entity.OrderRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * TODO(person-B): implement with JdbcTemplate. Keep SQL here, not scattered
 * across services. Consider whether inserts should be batched during
 * benchmarking runs (probably yes) vs. one-at-a-time in normal operation
 * (fine for this scale).
 */
@Repository
public class OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(OrderRow order) {
        throw new UnsupportedOperationException("TODO: INSERT OR REPLACE INTO orders ...");
    }

    public Optional<OrderRow> findById(String id) {
        throw new UnsupportedOperationException("TODO: SELECT ... WHERE id = ?");
    }

    public List<OrderRow> findBySymbol(String symbol) {
        throw new UnsupportedOperationException("TODO: SELECT ... WHERE symbol = ?");
    }
}
