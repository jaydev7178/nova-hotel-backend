-- V5__Update_order_items_schema.sql
-- Update order_items table to support cart functionality
-- Make order_id nullable and add user_id column

USE nova_hotel_db;
GO

-- Add user_id column to order_items table
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE name = 'user_id' AND object_id = OBJECT_ID('dbo.order_items'))
BEGIN
    ALTER TABLE dbo.order_items ADD user_id BIGINT NOT NULL;
END
GO

-- Add foreign key constraint for user_id
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = 'fk_order_items_user')
BEGIN
    ALTER TABLE dbo.order_items ADD CONSTRAINT fk_order_items_user FOREIGN KEY (user_id) REFERENCES dbo.users(id) ON DELETE CASCADE;
END
GO

-- Make order_id nullable
IF EXISTS (SELECT 1 FROM sys.columns WHERE name = 'order_id' AND object_id = OBJECT_ID('dbo.order_items') AND is_nullable = 0)
BEGIN
    ALTER TABLE dbo.order_items ALTER COLUMN order_id BIGINT NULL;
END
GO

-- Create index for user_id
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_order_items_user' AND object_id = OBJECT_ID('dbo.order_items'))
BEGIN
    CREATE INDEX idx_order_items_user ON dbo.order_items(user_id);
END
GO