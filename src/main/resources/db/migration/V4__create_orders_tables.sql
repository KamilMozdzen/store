CREATE TABLE customer_orders(
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  customer_name VARCHAR(120) NOT NULL,
  customer_email VARCHAR(255) NOT NULL,
  status VARCHAR(30) NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL,
  created_at TIMESTAMP(6) NOT NULL
);
CREATE TABLE order_items (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 order_id BIGINT NOT NULL,
 product_id BIGINT NOT NULL,
 product_name VARCHAR(120) NOT NULL,
 unit_price DECIMAL(12, 2) NOT NULL,
 quantity INT NOT NULL,
 line_total DECIMAL(12, 2) NOT NULL,

 CONSTRAINT fk_order_items_order
 FOREIGN KEY (order_id)
 REFERENCES customer_orders(id)
 ON DELETE CASCADE,

 CONSTRAINT fk_order_items_product
 FOREIGN KEY (product_id)
 REFERENCES products(id)
);