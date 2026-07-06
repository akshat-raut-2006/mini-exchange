package com.matchingengine.api.persistence.repository;

import com.matchingengine.api.persistence.entity.TradeRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TradeRepository {

    private final JdbcTemplate jdbcTemplate;

    public TradeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(TradeRow trade) {
        throw new UnsupportedOperationException("TODO: INSERT INTO trades ...");
    }

    public List<TradeRow> findBySymbol(String symbol) {
        throw new UnsupportedOperationException("TODO: SELECT ... WHERE symbol = ? ORDER BY executed_at DESC");
    }
}
