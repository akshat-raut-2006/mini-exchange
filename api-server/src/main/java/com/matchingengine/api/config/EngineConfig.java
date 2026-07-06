package com.matchingengine.api.config;

import com.matchingengine.core.engine.MatchingEngine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The MatchingEngine is stateful in-memory (the order book lives here) so it
 * must be a singleton shared across all requests, not created per-request.
 */
@Configuration
public class EngineConfig {

    @Bean
    public MatchingEngine matchingEngine() {
        return new MatchingEngine();
    }
}
