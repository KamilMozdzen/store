package com.project.store.order.exception;

import com.project.store.order.entity.OrderStatus;

public class InvalidOrderStatusTransitionException extends RuntimeException {
    public InvalidOrderStatusTransitionException(
            OrderStatus current,
            OrderStatus target
    ) {
        super("Cannot change order status from " + current + " to " + target);
    }
}
