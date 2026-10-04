package com.project.store.order.service;

import com.project.store.order.dto.OrderCreateRequest;
import com.project.store.order.dto.OrderItemRequest;
import com.project.store.order.dto.OrderStatusUpdateRequest;
import com.project.store.order.entity.CustomerOrder;
import com.project.store.order.entity.OrderItem;
import com.project.store.order.entity.OrderStatus;
import com.project.store.order.repository.CustomerOrderRepository;
import com.project.store.product.entity.Product;
import com.project.store.product.exception.InsufficientStockException;
import com.project.store.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
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
    @Test
    void returnsPagedOrderSummaries() {
        CustomerOrder order = org.mockito.Mockito.mock(CustomerOrder.class);
        Instant createdAt = Instant.parse("2026-09-29T10:00:00Z");

        when(order.getId()).thenReturn(10L);
        when(order.getCustomerName()).thenReturn("Jan Kowalski");
        when(order.getCustomerEmail()).thenReturn("jan@example.com");
        when(order.getStatus()).thenReturn(OrderStatus.NEW);
        when(order.getTotalAmount())
                .thenReturn(new BigDecimal("399.98"));
        when(order.getCreatedAt()).thenReturn(createdAt);

        var pageable = PageRequest.of(0, 20);

        when(orderRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(
                        List.of(order),
                        pageable,
                        1
                ));

        var response = orderService.findAll(pageable);

        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isEqualTo(1);
        assertThat(response.content()).hasSize(1);
        assertThat(response.content().getFirst().id()).isEqualTo(10L);
        assertThat(response.content().getFirst().status())
                .isEqualTo(OrderStatus.NEW);
        assertThat(response.content().getFirst().totalAmount())
                .isEqualByComparingTo("399.98");
    }
    @Test
    void returnsOrderDetails() {
        CustomerOrder order = org.mockito.Mockito.mock(CustomerOrder.class);
        OrderItem item = org.mockito.Mockito.mock(OrderItem.class);
        Product product = org.mockito.Mockito.mock(Product.class);
        Instant createdAt = Instant.parse("2026-09-29T10:00:00Z");

        when(order.getId()).thenReturn(10L);
        when(order.getCustomerName()).thenReturn("Jan Kowalski");
        when(order.getCustomerEmail()).thenReturn("jan@example.com");
        when(order.getStatus()).thenReturn(OrderStatus.NEW);
        when(order.getTotalAmount())
                .thenReturn(new BigDecimal("399.98"));
        when(order.getCreatedAt()).thenReturn(createdAt);
        when(order.getItems()).thenReturn(List.of(item));

        when(item.getProduct()).thenReturn(product);
        when(product.getId()).thenReturn(1L);
        when(item.getProductName()).thenReturn("Klawiatura");
        when(item.getUnitPrice())
                .thenReturn(new BigDecimal("199.99"));
        when(item.getQuantity()).thenReturn(2);
        when(item.getLineTotal())
                .thenReturn(new BigDecimal("399.98"));

        when(orderRepository.findDetailedById(10L))
                .thenReturn(Optional.of(order));

        var response = orderService.findById(10L);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().productId()).isEqualTo(1L);
        assertThat(response.items().getFirst().productName())
                .isEqualTo("Klawiatura");
        assertThat(response.items().getFirst().lineTotal())
                .isEqualByComparingTo("399.98");
    }
    @Test
    void restoresStockWhenOrderIsCancelled() {
        Product product = org.mockito.Mockito.mock(Product.class);

        when(product.getId()).thenReturn(1L);
        when(product.getName()).thenReturn("Klawiatura");
        when(product.getPrice()).thenReturn(new BigDecimal("199.99"));

        CustomerOrder order = new CustomerOrder(
                "Jan Kowalski",
                "jan@example.com"
        );
        order.addProduct(product, 2);

        when(orderRepository.findDetailedByIdForUpdate(10L))
                .thenReturn(Optional.of(order));

        var response = orderService.updateStatus(
                10L,
                new OrderStatusUpdateRequest(OrderStatus.CANCELLED)
        );

        assertThat(response.status())
                .isEqualTo(OrderStatus.CANCELLED);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().quantity()).isEqualTo(2);

        verify(product).increaseStock(2);
    }
}
