ALTER TABLE customers ADD COLUMN supabase_user_id VARCHAR(128);
ALTER TABLE customers DROP CONSTRAINT customers_phone_key;
CREATE INDEX idx_customers_phone ON customers (phone);
CREATE UNIQUE INDEX uq_customers_supabase_user_id ON customers (supabase_user_id) WHERE supabase_user_id IS NOT NULL;
