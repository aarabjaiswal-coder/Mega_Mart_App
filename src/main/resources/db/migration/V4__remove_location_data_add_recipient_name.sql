ALTER TABLE orders
    DROP CONSTRAINT IF EXISTS chk_order_dispatch_location_pair,
    DROP CONSTRAINT IF EXISTS chk_orders_delivery_coordinates_pair,
    DROP CONSTRAINT IF EXISTS chk_orders_delivery_latitude_range,
    DROP CONSTRAINT IF EXISTS chk_orders_delivery_longitude_range,
    DROP COLUMN IF EXISTS delivery_start_latitude,
    DROP COLUMN IF EXISTS delivery_start_longitude,
    DROP COLUMN IF EXISTS delivery_latitude,
    DROP COLUMN IF EXISTS delivery_longitude,
    ADD COLUMN recipient_name VARCHAR(120);

UPDATE orders
SET recipient_name = customers.phone
FROM customers
WHERE orders.customer_id = customers.id
  AND orders.recipient_name IS NULL;

ALTER TABLE orders
    ALTER COLUMN recipient_name SET NOT NULL;
