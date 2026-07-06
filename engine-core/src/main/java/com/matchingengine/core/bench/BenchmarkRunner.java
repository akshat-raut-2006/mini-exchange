package com.matchingengine.core.bench;

import com.matchingengine.core.engine.MatchingEngine;

/**
 * Standalone entry point: run with
 *   mvn exec:java -Dexec.mainClass="com.matchingengine.core.bench.BenchmarkRunner"
 *
 * TODO(person-A): this is the single biggest "wow factor per hour invested"
 * piece per the project scope — don't skip it. Minimum viable version:
 *  - submit N orders single-threaded, measure orders/sec and p50/p99 latency
 *    per submitOrder() call (use System.nanoTime(), store in a long[], sort
 *    and percentile at the end — avoid measurement overhead skewing results)
 *  - then repeat with multiple concurrent producer threads to see how
 *    throughput/latency change under contention
 *  - print a short human-readable summary; consider also writing CSV for
 *    a chart in the README
 */
public class BenchmarkRunner {

    public static void main(String[] args) {
        MatchingEngine engine = new MatchingEngine();
        // TODO: wire up LoadGenerator, warm up the JVM, then time the real run
        throw new UnsupportedOperationException("TODO: implement benchmark run + reporting");
    }
}
