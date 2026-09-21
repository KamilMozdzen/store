CREATE TABLE products (
   id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
   name VARCHAR (120) NOT NULL,
   description TEXT,
   price DECIMAL(12, 2)NOT NULL,
   stock_quantity INT NOT NULL
);