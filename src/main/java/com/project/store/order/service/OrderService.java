package com.project.store.order.service;

import com.project.store.order.dto.OrderCreateRequest;
import com.project.store.order.dto.OrderItemRequest;
import com.project.store.order.dto.OrderItemResponse;
import com.project.store.order.dto.OrderResponse;
import com.project.store.order.entity.CustomerOrder;
import com.project.store.order.repository.CustomerOrderRepository;
import com.project.store.product.entity.Product;
import com.project.store.product.exception.ProductNotFoundException;
import com.project.store.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;


@Service
public class OrderService {
    private final CustomerOrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(CustomerOrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        CustomerOrder order = new CustomerOrder(
            request.customerName().trim(),
            request.customerEmail().trim().toLowerCase(Locale.ROOT)
        );

        Map<Long, Integer> quantities = aggregateItems(request);

        quantities.forEach((productId, quantity) ->{
            Product product = productRepository.findByIdForUpdate(productId).
                    orElseThrow(() -> new ProductNotFoundException(productId));

            product.decreaseStock(quantity);
            order.addProduct(product, quantity);
        });
        CustomerOrder savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }
    private Map<Long, Integer> aggregateItems(OrderCreateRequest request) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();

        for(OrderItemRequest item : request.items()){
            quantities.merge(item.productId(),
                             item.quantity(),
                             Integer::sum);
        }
        return quantities;
    }

    private OrderResponse toResponse(CustomerOrder order) {
        var items = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getId(),
                        item.getProductName(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getLineTotal()

                )).toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                items
        );
    }

}
