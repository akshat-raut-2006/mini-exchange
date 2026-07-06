package com.matchingengine.api.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * TODO(person-B): use @WebMvcTest or @SpringBootTest + MockMvc/TestRestTemplate.
 * Cover: submit valid order returns 200/201 with expected body; submit
 * order that fully matches returns trades; cancel of unknown id returns
 * 404; book snapshot reflects resting orders.
 */
class OrderControllerTest {

    @Test
    @Disabled("TODO: implement once OrderService.submitOrder works")
    void submittingValidOrderReturnsOrderResponse() {
    }

    @Test
    @Disabled("TODO: implement")
    void cancellingUnknownOrderReturnsNotFound() {
    }

    @Test
    @Disabled("TODO: implement")
    void bookSnapshotReflectsRestingOrders() {
    }
}
