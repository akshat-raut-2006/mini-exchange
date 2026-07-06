package com.matchingengine.core.model;

/** Limit orders rest on the book; market orders execute immediately against it. */
public enum OrderType {
    LIMIT,
    MARKET
}
