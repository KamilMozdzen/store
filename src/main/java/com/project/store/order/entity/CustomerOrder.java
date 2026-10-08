package com.project.store.order.entity;

import com.project.store.order.exception.InvalidOrderStatusTransitionException;
import com.project.store.product.entity.Product;
import com.project.store.user.entity.AppUser;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customer_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 255)
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    public CustomerOrder(String customerName, String customerEmail){
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.status = OrderStatus.NEW;
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = Instant.now();
    }

    public CustomerOrder(AppUser user){
        this.user = user;
        this.customerName = user.getFirstName() + " " + user.getLastName().trim();
        this.customerEmail = user.getEmail();
        this.status = OrderStatus.NEW;
        this.totalAmount = BigDecimal.ZERO;
        this.createdAt = Instant.now();
    }
    public void addProduct(Product product, int quantity){
        OrderItem item = new OrderItem(this, product, quantity);
        items.add(item);
        totalAmount = totalAmount.add(item.getLineTotal());
    }
    public void changeStatus(OrderStatus target){
        if(status == target){
            return;
        }
        boolean allowed = switch(status){
            case NEW -> target == OrderStatus.PAID
                    || target == OrderStatus.CANCELLED;
            case PAID -> target == OrderStatus.SHIPPED
                    || target == OrderStatus.CANCELLED;
            case SHIPPED,CANCELLED -> false;
        };
        if(!allowed){
            throw new InvalidOrderStatusTransitionException(status,target);
        }
        status = target;
    }
}
