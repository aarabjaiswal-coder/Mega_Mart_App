ALTER TABLE products
    ADD COLUMN offer_price NUMERIC(10, 2);

ALTER TABLE products
    ADD CONSTRAINT chk_products_offer_price
        CHECK (offer_price IS NULL OR (offer_price > 0 AND offer_price < price));
