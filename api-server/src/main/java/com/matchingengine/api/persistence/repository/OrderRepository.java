package com.matchingengine.api.persistence.repository;

import com.matchingengine.api.persistence.entity.OrderRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<OrderRow> ROW_MAPPER = (rs, rowNum) -> new OrderRow(
            rs.getString("id"),
            rs.getString("symbol"),
            rs.getString("side"),
            rs.getString("type"),
            rs.getString("price") == null ? null : new BigDecimal(rs.getString("price")),
            new BigDecimal(rs.getString("quantity")),
            new BigDecimal(rs.getString("remaining_quantity")),
            rs.getString("status"),
            Instant.parse(rs.getString("created_at"))
    );

    public void save(OrderRow order) {
        // SQLite's "INSERT OR REPLACE" has no direct Postgres equivalent;
        // this is the Postgres upsert form (requires the primary key
        // constraint on `id`, which schema.sql already declares).
        jdbcTemplate.update(
                """
                INSERT INTO orders
                    (id, symbol, side, type, price, quantity, remaining_quantity, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    symbol = EXCLUDED.symbol,
                    side = EXCLUDED.side,
                    type = EXCLUDED.type,
                    price = EXCLUDED.price,
                    quantity = EXCLUDED.quantity,
                    remaining_quantity = EXCLUDED.remaining_quantity,
                    status = EXCLUDED.status,
                    created_at = EXCLUDED.created_at
                """,
                order.id(),
                order.symbol(),
                order.side(),
                order.type(),
                order.price() == null ? null : order.price().toPlainString(),
                order.quantity().toPlainString(),
                order.remainingQuantity().toPlainString(),
                order.status(),
                order.createdAt().toString()
        );
    }

    public Optional<OrderRow> findById(String id) {
        List<OrderRow> results = jdbcTemplate.query(
                "SELECT * FROM orders WHERE id = ?", ROW_MAPPER, id
        );
        return results.stream().findFirst();
    }

    public List<OrderRow> findBySymbol(String symbol) {
        return jdbcTemplate.query(
                "SELECT * FROM orders WHERE symbol = ?", ROW_MAPPER, symbol
        );
    }
}