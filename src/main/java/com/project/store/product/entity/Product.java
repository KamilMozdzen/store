package com.project.store.product.entity;

import com.project.store.category.entity.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Setter
    @Column(name = "description", columnDefinition = "Text")
    private String description;

    @Setter
    @Column(name = "price",precision = 12 ,scale = 2, nullable = false)
    private BigDecimal price;

    @Setter
    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id" , nullable = false)
    private Category category;

    public Product(String name, String description, BigDecimal price, int stockQuantity, Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.category = category;
    }

}
