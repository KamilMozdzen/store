package com.project.store.cart.service;

import com.project.store.cart.dto.CartItemAddRequest;
import com.project.store.cart.dto.CartItemQuantityRequest;
import com.project.store.cart.dto.CartItemResponse;
import com.project.store.cart.dto.CartResponse;
import com.project.store.cart.entity.CartItem;
import com.project.store.cart.entity.ShoppingCart;
import com.project.store.cart.exception.CartItemNotFoundException;
import com.project.store.cart.repository.ShoppingCartRepository;
import com.project.store.product.entity.Product;
import com.project.store.product.exception.InsufficientStockException;
import com.project.store.product.exception.ProductNotFoundException;
import com.project.store.product.repository.ProductRepository;
import com.project.store.user.entity.AppUser;
import com.project.store.user.exception.InvalidCredentialsException;
import com.project.store.user.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
@Service
@Transactional(readOnly = true)
public class ShoppingCartService {

    private final ShoppingCartRepository cartRepository;
    private final AppUserRepository userRepository;
    private final ProductRepository productRepository;

    public ShoppingCartService(
            ShoppingCartRepository cartRepository,
            AppUserRepository userRepository,
            ProductRepository productRepository
    ){
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public CartResponse getCart(String authenticatedEmail){
        ShoppingCart cart = cartRepository
                .findDetailedByUserEmail(authenticatedEmail)
                .orElseGet(()-> createCart(authenticatedEmail));

        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(
            String authenticatedEmail,
            CartItemAddRequest request
    ){
        ShoppingCart cart = findForUpdateOrCreate(authenticatedEmail);

        Product product = productRepository
                .findById(request.productId())
                .orElseThrow(()-> new ProductNotFoundException(request.productId()));

        cart.addProduct(product, request.quantity());

        CartItem item = findItem(cart, request.productId());
        validateStock(item);

        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateQuantity(
            String authenticatedEmail,
            Long productId,
            CartItemQuantityRequest request
    ){
        ShoppingCart cart = cartRepository
                .findDetailedByUserEmailForUpdate(authenticatedEmail)
                .orElseThrow(()-> new CartItemNotFoundException(productId));

        cart.updateQuantity(productId, request.quantity());

        CartItem item = findItem(cart, productId);
        validateStock(item);

        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItem(
            String authenticatedEmail,
            Long productId
    ){
        ShoppingCart cart = cartRepository
                .findDetailedByUserEmailForUpdate(authenticatedEmail)
                .orElseThrow(()-> new CartItemNotFoundException(productId));

        cart.removeProduct(productId);

        return toResponse(cartRepository.save(cart));
    }

    @Transactional
    public void clear(String authenticatedEmail){
        ShoppingCart cart = cartRepository
                .findDetailedByUserEmailForUpdate(authenticatedEmail)
                .orElse(null);

        if(cart != null){
            cart.clear();
            cartRepository.save(cart);
        }
    }

    private ShoppingCart findForUpdateOrCreate(String authenticatedEmail){
        return cartRepository
                .findDetailedByUserEmailForUpdate(authenticatedEmail)
                .orElseGet(()-> createCart(authenticatedEmail));
    }

    private ShoppingCart createCart(String authenticatedEmail){
        AppUser user = userRepository
                .findByEmailIgnoreCase(authenticatedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        return cartRepository.save(new ShoppingCart(user));
    }

    private CartItem findItem(ShoppingCart cart, Long productId){
        return cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId)
                )
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(productId));
    }

    private void validateStock(CartItem item){
        Product product = item.getProduct();

        if(item.getQuantity() > product.getStockQuantity()){
            throw new InsufficientStockException(
                    product.getId(),
                    product.getStockQuantity(),
                    item.getQuantity()
            );
        }
    }

    private CartResponse toResponse(ShoppingCart cart){
        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cart.getId(),
                items,
                totalAmount,
                cart.getUpdatedAt()
        );
    }

    private CartItemResponse toItemResponse(CartItem item){
        Product product = item.getProduct();

        BigDecimal lineTotal = product.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new CartItemResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                item.getQuantity(),
                lineTotal,
                product.getStockQuantity()
        );
    }
}
