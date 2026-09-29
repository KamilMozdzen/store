package com.project.store.order.service;

import com.project.store.order.dto.OrderCreateRequest;
import com.project.store.order.dto.OrderItemRequest;
import com.project.store.order.entity.CustomerOrder;
import com.project.store.order.repository.CustomerOrderRepository;
import com.project.store.product.entity.Product;
import com.project.store.product.exception.InsufficientStockException;
import com.project.store.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private CustomerOrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createsOrderAndAggregatesRepeatedProducts() {
        Product product = org.mockito.Mockito.mock(Product.class);

        when(product.getId()).thenReturn(1L);
        when(product.getName()).thenReturn("Klawiatura");
        when(product.getPrice()).thenReturn(new BigDecimal("199.99"));

        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        when(orderRepository.save(any(CustomerOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderCreateRequest request = new OrderCreateRequest(
                "Jan Kowalski",
                "JAN@EXAMPLE.COM",
                List.of(new OrderItemRequest(1L,1),
                        new OrderItemRequest(1L,2)
                )
        );
        var response = orderService.create(request);

        assertThat(response.customerName()).isEqualTo("Jan Kowalski");
        assertThat(response.customerEmail()).isEqualTo("jan@example.com");
        assertThat(response.totalAmount()).isEqualByComparingTo("599.97");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().quantity()).isEqualTo(3);
        assertThat(response.items().getFirst().lineTotal()).isEqualByComparingTo("599.97");

        verify(productRepository).findByIdForUpdate(1L);
        verify(product).decreaseStock(3);
        verify(orderRepository).save(any(CustomerOrder.class));
    }

    @Test
    void doesNotSaveOrderWhenStockIsInsufficient() {
        Product product = org.mockito.Mockito.mock(Product.class);

        when(productRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(product));

        doThrow(new InsufficientStockException(1L,1,2))
                .when(product)
                .decreaseStock(2);

        OrderCreateRequest request = new OrderCreateRequest(
                "Jan Kowalski",
                "jan@example.com",
                List.of(new OrderItemRequest(1L,2))
        );

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessage("Product with id 1 has only 1 item available, requested 2");

        verify(orderRepository, never()).save(any(CustomerOrder.class));
    }
}
