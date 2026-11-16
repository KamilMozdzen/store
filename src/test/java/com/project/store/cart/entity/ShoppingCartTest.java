package com.project.store.cart.entity;

import com.project.store.product.entity.Product;
import com.project.store.user.entity.AppUser;
import org.junit.jupiter.api.Test;
import com.project.store.cart.exception.CartItemNotFoundException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShoppingCartTest {

    @Test
    void addsNewProduct() {
        ShoppingCart cart = new ShoppingCart(mock(AppUser.class));
        Product product = product(1L);

        cart.addProduct(product, 2);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().getFirst().getProduct())
                .isEqualTo(product);
        assertThat(cart.getItems().getFirst().getQuantity())
                .isEqualTo(2);
    }

    @Test
    void aggregatesRepeatedProduct(){
        ShoppingCart cart = new ShoppingCart(mock(AppUser.class));
        Product product = product(1L);

        cart.addProduct(product, 2);
        cart.addProduct(product, 3);

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().getFirst().getQuantity()).isEqualTo(5);
    }

    @Test
    void updatesProductQuantity() {
        ShoppingCart cart = new ShoppingCart(mock(AppUser.class));
        Product product = product(1L);

        cart.addProduct(product, 2);
        cart.updateQuantity(1L, 6);

        assertThat(cart.getItems().getFirst().getQuantity())
                .isEqualTo(6);
    }
    @Test
    void removeProduct(){
        ShoppingCart cart = new ShoppingCart(mock(AppUser.class));

        Product product = product(1L);

        cart.addProduct(product, 2);
        cart.removeProduct(1L);

        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    void rejectsInvalidQuantity(){
        ShoppingCart cart = new ShoppingCart(mock(AppUser.class));
        Product product = product(1L);

        assertThatThrownBy(() -> cart.addProduct(product, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantity must be greater than zero");
    }

    @Test
    void rejectsUpdatingMissingProduct(){
        ShoppingCart cart = new ShoppingCart(mock(AppUser.class));

        assertThatThrownBy(() -> cart.updateQuantity(99L, 2)
        ).isInstanceOf(CartItemNotFoundException.class)
                .hasMessage("Product with id 99 is not present in cart");
    }
    private Product product(Long id){
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(id);
        return product;
    }

}
