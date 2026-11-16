CREATE TABLE shopping_carts(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,

    CONSTRAINT uk_shopping_carts_user UNIQUE (user_id),
    CONSTRAINT fk_shopping_carts_user
                           FOREIGN KEY (user_id)
                           REFERENCES  app_users(id)
                           ON DELETE CASCADE
);
CREATE TABLE cart_items(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,

    CONSTRAINT uk_cart_items_cart_product
                    UNIQUE(cart_id, product_id),

    CONSTRAINT fk_cart_items_cart
        FOREIGN KEY (cart_id)
            REFERENCES shopping_carts(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_cart_items_product
        FOREIGN KEY (product_id)
            REFERENCES products(id)
            ON DELETE CASCADE,

    CONSTRAINT chk_cart_items_quantity
        CHECK (quantity > 0)

);
CREATE INDEX idx_cart_items_product
    ON cart_items(product_id);