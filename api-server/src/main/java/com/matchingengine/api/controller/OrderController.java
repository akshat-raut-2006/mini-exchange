package com.matchingengine.api.controller;

import com.matchingengine.api.auth.JwtAuthFilter;
import com.matchingengine.api.dto.BookSnapshotResponse;
import com.matchingengine.api.dto.OrderRequest;
import com.matchingengine.api.dto.OrderResponse;
import com.matchingengine.api.persistence.entity.OrderRow;
import com.matchingengine.api.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> submitOrder(@RequestBody OrderRequest request,
                                                       HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute(JwtAuthFilter.USER_ID_ATTR);
        String userEmail = (String) httpRequest.getAttribute(JwtAuthFilter.USER_EMAIL_ATTR);
        OrderResponse response = orderService.submitOrder(request, userId, userEmail);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable("orderId") String orderId,
                                             @RequestParam("symbol") String symbol) {
        boolean cancelled = orderService.cancelOrder(symbol, orderId);
        return cancelled ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/book")
    public ResponseEntity<BookSnapshotResponse> getBook(@RequestParam("symbol") String symbol) {
        return ResponseEntity.ok(orderService.getBookSnapshot(symbol));
    }

    /** Order history for whoever's currently logged in - requires a valid bearer token. */
    @GetMapping("/mine")
    public ResponseEntity<List<OrderRow>> getMyOrders(HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute(JwtAuthFilter.USER_ID_ATTR);
        return ResponseEntity.ok(orderService.getOrdersForUser(userId));
    }
}
