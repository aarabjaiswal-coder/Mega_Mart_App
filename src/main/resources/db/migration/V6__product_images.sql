CREATE TABLE product_images (
    product_id BIGINT PRIMARY KEY REFERENCES products(id) ON DELETE CASCADE,
    content_type VARCHAR(64) NOT NULL,
    image_data BYTEA NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
