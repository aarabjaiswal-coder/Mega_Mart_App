ALTER TABLE orders
    ADD COLUMN out_for_delivery_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN delivery_start_latitude DOUBLE PRECISION,
    ADD COLUMN delivery_start_longitude DOUBLE PRECISION,
    ADD CONSTRAINT chk_order_dispatch_location_pair
        CHECK ((delivery_start_latitude IS NULL) = (delivery_start_longitude IS NULL));
