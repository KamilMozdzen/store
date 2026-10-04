package com.project.store.order.entity;

import com.project.store.order.exception.InvalidOrderStatusTransitionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerOrderTest {

    @Test
    void changesStatusFromNewToPaidAndThenShipped() {
        CustomerOrder order = new CustomerOrder(
                "Jan Kowalski",
                "jan@example.com"
        );

        order.changeStatus(OrderStatus.PAID);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);

        order.changeStatus(OrderStatus.SHIPPED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void allowsCancellingNewOrder() {
        CustomerOrder order = new CustomerOrder(
                "Jan Kowalski",
                "jan@example.com"
        );

        order.changeStatus(OrderStatus.CANCELLED);

        assertThat(order.getStatus())
                .isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void rejectsShippingUnpaidOrder() {
        CustomerOrder order = new CustomerOrder(
                "Jan Kowalski",
                "jan@example.com"
        );

        assertThatThrownBy(() ->
                order.changeStatus(OrderStatus.SHIPPED)
        )
                .isInstanceOf(
                        InvalidOrderStatusTransitionException.class
                )
                .hasMessage(
                        "Cannot change order status from NEW to SHIPPED"
                );

        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
    }

    @Test
    void rejectsChangingCancelledOrder() {
        CustomerOrder order = new CustomerOrder(
                "Jan Kowalski",
                "jan@example.com"
        );
        order.changeStatus(OrderStatus.CANCELLED);

        assertThatThrownBy(() ->
                order.changeStatus(OrderStatus.PAID)
        )
                .isInstanceOf(
                        InvalidOrderStatusTransitionException.class
                )
                .hasMessage(
                        "Cannot change order status from CANCELLED to PAID"
                );
    }
}