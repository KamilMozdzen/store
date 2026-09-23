INSERT INTO categories (name)
SELECT 'Uncategorized'
WHERE NOT EXISTS (
    SELECT 1
    FROM categories
    WHERE name = 'Uncategorized'
);
ALTER TABLE products
    ADD COLUMN category_id BIGINT NULL;

UPDATE products
SET category_id = (
    SELECT id
    FROM categories
    WHERE name = 'Uncategorized'
    LIMIT 1
)
WHERE category_id IS NULL;

ALTER TABLE products
    MODIFY category_id BIGINT NOT NULL,
        ADD CONSTRAINT  fk_products_category
            FOREIGN KEY (category_id) REFERENCES categories(id);