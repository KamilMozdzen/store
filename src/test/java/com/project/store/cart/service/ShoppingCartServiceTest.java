package com.project.store.cart.service;

import com.project.store.cart.dto.CartItemAddRequest;
import com.project.store.cart.dto.CartItemQuantityRequest;
import com.project.store.cart.entity.ShoppingCart;
import com.project.store.cart.exception.CartItemNotFoundException;
import com.project.store.cart.repository.ShoppingCartRepository;
import com.project.store.product.entity.Product;
import com.project.store.product.exception.InsufficientStockException;
import com.project.store.product.repository.ProductRepository;
import com.project.store.user.entity.AppUser;
import com.project.store.user.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShoppingCartServiceTest {

    @Mock
    private ShoppingCartRepository cartRepository;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ShoppingCartService cartService;

    @Test
    void createEmptyCartWhenUserHasNoCart(){
        AppUser user = mock(AppUser.class);

        when(cartRepository.findDetailedByUserEmail(
                "jan@example.com"
        )).thenReturn(Optional.empty());

        when(userRepository.findByEmailIgnoreCase(
                "jan@example.com"
        )).thenReturn(Optional.of(user));

        when(cartRepository.save(any(ShoppingCart.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        var response = cartService.getCart(
                "jan@example.com"
        );

        assertThat(response.items()).isEmpty();
        assertThat(response.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.updatedAt()).isNotNull();

        verify(cartRepository).save(any(ShoppingCart.class));
    }

    @Test
    void addsProductToCart(){
        AppUser user = mock(AppUser.class);
        Product product = product(1L,10);

        when(cartRepository.findDetailedByUserEmailForUpdate(
                "jan@example.com"
        )).thenReturn(Optional.empty());

        when(userRepository.findByEmailIgnoreCase(
                "jan@example.com"
        )).thenReturn(Optional.of(user));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        when(cartRepository.save(any(ShoppingCart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = cartService.addItem(
                "jan@example.com",
                new CartItemAddRequest(1L,2)
        );

        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().productId()).isEqualTo(1L);
        assertThat(response.items().getFirst().quantity()).isEqualTo(2);
        assertThat(response.items().getFirst().lineTotal()).isEqualByComparingTo("399.98");
        assertThat(response.totalAmount()).isEqualByComparingTo("399.98");
    }

    @Test
    void rejectsQuantityGreaterThanAvailableStock(){
        ShoppingCart cart = new ShoppingCart(mock(AppUser.class));

        Product product = mock(Product.class);

        when(product.getId()).thenReturn(1L);
        when(product.getStockQuantity()).thenReturn(1);

        when(cartRepository.findDetailedByUserEmailForUpdate(
                "jan@example.com"
        )).thenReturn(Optional.of(cart));

        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        assertThatThrownBy(()-> cartService.addItem(
                "jan@example.com",
                new CartItemAddRequest(1L, 2)
        )).isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void rejectsUpdatingItemWhenDoesNotExist(){
        when(cartRepository.findDetailedByUserEmailForUpdate(
                "jan@example.com"
        ))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateQuantity(
                "jan@example.com",
                99L,
                new CartItemQuantityRequest(2)
        )).isInstanceOf(CartItemNotFoundException.class)
                .hasMessage("Product with id 99 is not present in cart");
    }

    private Product product(Long id, int stockQuantity){
        Product product = mock(Product.class);

        when(product.getId()).thenReturn(id);
        when(product.getName()).thenReturn("Klawiatura");
        when(product.getPrice()).thenReturn(new BigDecimal("199.99"));
        when(product.getStockQuantity()).thenReturn(stockQuantity);

        return product;
    }



}
