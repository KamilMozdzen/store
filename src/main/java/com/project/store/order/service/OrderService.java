package com.project.store.order.service;

import com.project.store.common.dto.PageResponse;
import com.project.store.order.dto.*;
import com.project.store.order.entity.CustomerOrder;
import com.project.store.order.exception.OrderNotFoundException;
import com.project.store.order.repository.CustomerOrderRepository;
import com.project.store.product.entity.Product;
import com.project.store.product.exception.ProductNotFoundException;
import com.project.store.product.repository.ProductRepository;
import com.project.store.order.dto.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import com.project.store.order.dto.OrderStatusUpdateRequest;
import com.project.store.order.entity.OrderStatus;
import com.project.store.user.entity.AppUser;
import com.project.store.user.exception.InvalidCredentialsException;
import com.project.store.user.repository.AppUserRepository;
import com.project.store.order.entity.ShippingAddress;
import com.project.store.cart.dto.CartCheckoutRequest;
import com.project.store.cart.entity.CartItem;
import com.project.store.cart.entity.ShoppingCart;
import com.project.store.cart.exception.EmptyCartException;
import com.project.store.cart.repository.ShoppingCartRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;

@Transactional(readOnly = true)
@Service
public class OrderService {
    private final CustomerOrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AppUserRepository userRepository;
    private final ShoppingCartRepository cartRepository;

    public OrderService(CustomerOrderRepository orderRepository,
                        ProductRepository productRepository,
                        AppUserRepository userRepository,
                        ShoppingCartRepository cartRepository)
    {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
    }

    @Transactional
    public OrderResponse create(OrderCreateRequest request,String authenticatedEmail) {
        AppUser user = userRepository
                .findByEmailIgnoreCase(authenticatedEmail)
                .orElseThrow(InvalidCredentialsException::new);


        ShippingAddress shippingAddress = toShippingAddress(
                request.shippingAddress()
        );

        CustomerOrder order = new CustomerOrder(
                user,
                shippingAddress
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



    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request) {
        CustomerOrder order = orderRepository
                .findDetailedByIdForUpdate(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        OrderStatus previousStatus = order.getStatus();
        order.changeStatus(request.status());

        if(request.status() == OrderStatus.CANCELLED
            && previousStatus != OrderStatus.CANCELLED){
            order.getItems().forEach(item ->
                    item.getProduct().increaseStock(item.getQuantity()));
        }
        return toResponse(order);
    }

    @Transactional
    public OrderResponse checkoutCart(CartCheckoutRequest request,
                                      String authenticatedEmail) {
        AppUser user = userRepository.findByEmailIgnoreCase(authenticatedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        ShoppingCart cart = cartRepository
                .findDetailedByUserEmailForUpdate(authenticatedEmail)
                .orElseThrow(EmptyCartException::new);

        if (cart.getItems().isEmpty()) {
            throw new EmptyCartException();
        }

        CustomerOrder order = new CustomerOrder(
                user,
                toShippingAddress(request.shippingAddress())
        );

        cart.getItems().stream()
                .sorted(Comparator.comparing(
                        item -> item.getProduct().getId()
                ))
                .forEach(cartItem ->
                        addCartItemToOrder(order, cartItem));

        CustomerOrder savedOrder = orderRepository.save(order);

        cart.clear();
        cartRepository.save(cart);

        return toResponse(savedOrder);
    }
    private ShippingAddressResponse toAddressResponse(
            ShippingAddress address
    ) {
        if (address == null) {
            return null;
        }

        return new ShippingAddressResponse(
                address.getRecipient(),
                address.getStreet(),
                address.getCity(),
                address.getPostalCode(),
                address.getCountryCode()
        );
    }

    public OrderResponse findById(
            Long id,
            String authenticatedEmail,
            boolean admin
    ) {
        CustomerOrder order = (
                admin
                        ? orderRepository.findDetailedById(id)
                        : orderRepository.findDetailedByIdAndUserEmail(
                        id,
                        authenticatedEmail
                )
        )
                .orElseThrow(() -> new OrderNotFoundException(id));

        return toResponse(order);
    }

    private OrderSummaryResponse toSummaryResponse(CustomerOrder order){
        return new OrderSummaryResponse(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
    }
    public PageResponse<OrderSummaryResponse> findAll(
            Pageable pageable,
            String authenticatedEmail,
            boolean admin
    ){
        Page<CustomerOrder> orders = admin
                ? orderRepository.findAll(pageable)
                : orderRepository.findAllByUserEmailIgnoreCase(
                authenticatedEmail, pageable);

        return PageResponse.from(orders.map(this::toSummaryResponse));
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
                toAddressResponse(order.getShippingAddress()),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                items
        );
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

    private ShippingAddress toShippingAddress(
            ShippingAddressRequest request
    ){
        return new ShippingAddress(
                request.recipient().trim(),
                request.street().trim(),
                request.city().trim(),
                request.postalCode().trim(),
                request.countryCode().trim().toUpperCase(Locale.ROOT)
        );
    }

    private void addCartItemToOrder(
            CustomerOrder order, CartItem cartItem){

        Long productId = cartItem.getProduct().getId();

        Product product = productRepository
                .findByIdForUpdate(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.decreaseStock(cartItem.getQuantity());

        order.addProduct(
                product,
                cartItem.getQuantity()
        );
    }

}
