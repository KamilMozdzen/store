ALTER TABLE customer_orders
    ADD COLUMN user_id BIGINT NULL AFTER id;

UPDATE customer_orders customer_order
    INNER JOIN app_users app_user
        ON LOWER(app_user.email) = LOWER(customer_order.customer_email)
SET customer_order.user_id = app_user.id;

CREATE INDEX idx_customer_orders_user_id
    ON customer_orders(user_id);

ALTER TABLE customer_orders
    ADD CONSTRAINT fk_customer_orders_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id);