package com.matchingengine.core.bench;

import com.matchingengine.core.model.Order;

import java.util.List;

/**
 * Generates a stream of synthetic orders for load testing the engine.
 *
 * TODO(person-A): generate a realistic-ish mix (e.g. random walk around a
 * mid price, mostly limit orders with a smaller fraction of market orders)
 * so the benchmark isn't trivially degenerate (e.g. all orders at one price).
 */
public class LoadGenerator {

    public List<Order> generate(int count, String symbol) {
        throw new UnsupportedOperationException("TODO: produce `count` synthetic orders for benchmarking");
    }
}
