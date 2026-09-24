package com.project.store.product.repository;


import com.project.store.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = "category")
    @Query("""
           SELECT product
           FROM Product product
           WHERE LOWER(product.name) LIKE LOWER(
                      CONCAT('%', :searchTerm, '%')
                      )
           AND (
                :categoryId IS NULL
                OR product.category.id = :categoryId
                      )
           """)
    Page<Product> search(
            @Param("searchTerm") String searchTerm,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );
}
