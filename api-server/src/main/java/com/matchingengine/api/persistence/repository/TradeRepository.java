package com.matchingengine.api.persistence.repository;

import com.matchingengine.api.persistence.entity.TradeRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public class TradeRepository {

    private final JdbcTemplate jdbcTemplate;

    public TradeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<TradeRow> ROW_MAPPER = (rs, rowNum) -> new TradeRow(
            rs.getString("trade_id"),
            rs.getString("symbol"),
            rs.getString("maker_order_id"),
            rs.getString("taker_order_id"),
            new BigDecimal(rs.getString("price")),
            new BigDecimal(rs.getString("quantity")),
            Instant.parse(rs.getString("executed_at"))
    );

    // Trades are immutable once created (a fill happened or it didn't), so a
    // plain INSERT is fine here - no need for INSERT OR REPLACE like orders.
    public void save(TradeRow trade) {
        jdbcTemplate.update(
                """
                INSERT INTO trades
                    (trade_id, symbol, maker_order_id, taker_order_id, price, quantity, executed_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                trade.tradeId(),
                trade.symbol(),
                trade.makerOrderId(),
                trade.takerOrderId(),
                trade.price().toPlainString(),
                trade.quantity().toPlainString(),
                trade.executedAt().toString()
        );
    }

    public List<TradeRow> findBySymbol(String symbol) {
        return jdbcTemplate.query(
                "SELECT * FROM trades WHERE symbol = ? ORDER BY executed_at DESC",
                ROW_MAPPER, symbol
        );
    }
}

