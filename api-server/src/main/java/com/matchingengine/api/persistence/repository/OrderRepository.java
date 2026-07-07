package com.matchingengine.api.persistence.repository;

import com.matchingengine.api.persistence.entity.OrderRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Stores/reads order rows. Prices and quantities are stored as TEXT (see
 * schema.sql for why) so we convert BigDecimal <-> String at the boundary.
 */
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

    /**
     * INSERT OR REPLACE means: if this order's id doesn't exist yet, insert it;
     * if it does (e.g. we're updating its status/remaining_quantity after a
     * fill), overwrite the existing row instead of erroring on a duplicate key.
     */
    public void save(OrderRow order) {
        jdbcTemplate.update(
                """
                INSERT OR REPLACE INTO orders
                    (id, symbol, side, type, price, quantity, remaining_quantity, status, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
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
