package com.matchingengine.api.controller;

import com.matchingengine.api.dto.BookSnapshotResponse;
import com.matchingengine.api.dto.OrderRequest;
import com.matchingengine.api.dto.OrderResponse;
import com.matchingengine.api.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> submitOrder(@RequestBody OrderRequest request) {
        OrderResponse response = orderService.submitOrder(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderId,
                                             @RequestParam String symbol) {
        boolean cancelled = orderService.cancelOrder(symbol, orderId);
        return cancelled ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/book")
    public ResponseEntity<BookSnapshotResponse> getBook(@RequestParam String symbol) {
        return ResponseEntity.ok(orderService.getBookSnapshot(symbol));
    }
}