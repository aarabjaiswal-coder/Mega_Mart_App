ALTER TABLE orders
    ADD COLUMN delivery_latitude DOUBLE PRECISION,
    ADD COLUMN delivery_longitude DOUBLE PRECISION,
    ADD CONSTRAINT chk_orders_delivery_coordinates_pair
        CHECK ((delivery_latitude IS NULL) = (delivery_longitude IS NULL)),
    ADD CONSTRAINT chk_orders_delivery_latitude_range
        CHECK (delivery_latitude IS NULL OR delivery_latitude BETWEEN -90 AND 90),
    ADD CONSTRAINT chk_orders_delivery_longitude_range
        CHECK (delivery_longitude IS NULL OR delivery_longitude BETWEEN -180 AND 180);
