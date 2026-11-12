ALTER TABLE customer_orders
    ADD COLUMN shipping_recipient VARCHAR(120) NULL,
    ADD COLUMN shipping_street VARCHAR(160) NULL,
    ADD COLUMN shipping_city VARCHAR(100) NULL,
    ADD COLUMN shipping_postal_code VARCHAR(20) NULL,
    ADD COLUMN shipping_country_code VARCHAR(2) NULL;