package com.matchingengine.api.controller;

import com.matchingengine.api.dto.BookSnapshotResponse;
import com.matchingengine.api.dto.OrderRequest;
import com.matchingengine.api.dto.OrderResponse;
import com.matchingengine.api.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * TODO(person-B):
 *  - POST /api/orders           submit a new order
 *  - DELETE /api/orders/{id}    cancel an order (symbol as query param for now)
 *  - GET /api/orders/book       current book snapshot for a symbol
 *  - add @Valid once OrderRequest has bean validation annotations
 *  - add error handling (@ExceptionHandler / @ControllerAdvice) for
 *    unknown order id, invalid symbol, etc. — decide on a consistent
 *    error response shape
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> submitOrder(@RequestBody OrderRequest request) {
        throw new UnsupportedOperationException("TODO: delegate to orderService.submitOrder");
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderId,
                                             @RequestParam String symbol) {
        throw new UnsupportedOperationException("TODO: delegate to orderService.cancelOrder");
    }

    @GetMapping("/book")
    public ResponseEntity<BookSnapshotResponse> getBook(@RequestParam String symbol) {
        throw new UnsupportedOperationException("TODO: delegate to orderService.getBookSnapshot");
    }
}
