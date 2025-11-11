-- Create database if not exists
-- Create database if it does not exist (T-SQL)
IF DB_ID(N'nova_hotel_db') IS NULL
BEGIN
    CREATE DATABASE nova_hotel_db;
END
GO

USE nova_hotel_db;
GO

-- Create users table
IF OBJECT_ID(N'dbo.users', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.users (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(50) UNIQUE NOT NULL,
        email NVARCHAR(100) UNIQUE NOT NULL,
        password NVARCHAR(255) NOT NULL,
        full_name NVARCHAR(100) NOT NULL,
        phone_number NVARCHAR(20),
        address NVARCHAR(MAX),
        role NVARCHAR(20) NOT NULL CONSTRAINT chk_users_role CHECK (role IN ('USER','OWNER','ADMIN')) DEFAULT 'USER',
        is_active BIT DEFAULT 1,
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE()
    );
END
GO

-- Create categories table
IF OBJECT_ID(N'dbo.categories', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.categories (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(100) UNIQUE NOT NULL,
        description NVARCHAR(MAX),
        is_active BIT DEFAULT 1,
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE()
    );
END
GO

-- Create products table
IF OBJECT_ID(N'dbo.products', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.products (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(200) NOT NULL,
        description NVARCHAR(MAX),
        price DECIMAL(10,2) NOT NULL,
        stock_quantity INT NOT NULL DEFAULT 0,
        sku NVARCHAR(100) UNIQUE,
        image_url NVARCHAR(500),
        is_active BIT DEFAULT 1,
        category_id BIGINT NOT NULL,
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES dbo.categories(id) ON DELETE CASCADE
    );
END
GO

-- Create orders table
IF OBJECT_ID(N'dbo.orders', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.orders (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        order_number NVARCHAR(50) UNIQUE NOT NULL,
        user_id BIGINT NOT NULL,
        status NVARCHAR(40) NOT NULL CONSTRAINT chk_orders_status CHECK (status IN ('PENDING','APPROVED','PAYMENT_INFO_SENT','PAYMENT_CONFIRMED','DELIVERY_INITIATED','DELIVERED','CANCELLED')) DEFAULT 'PENDING',
        total_amount DECIMAL(10,2) NOT NULL,
        shipping_address NVARCHAR(MAX),
        notes NVARCHAR(MAX),
        payment_info NVARCHAR(MAX),
        terms_accepted BIT DEFAULT 0,
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES dbo.users(id) ON DELETE CASCADE
    );
END
GO

-- Create order_items table
IF OBJECT_ID(N'dbo.order_items', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.order_items (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        order_id BIGINT NOT NULL,
        product_id BIGINT NOT NULL,
        quantity INT NOT NULL,
        unit_price DECIMAL(10,2) NOT NULL,
        total_price DECIMAL(10,2) NOT NULL,
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES dbo.orders(id) ON DELETE CASCADE,
        CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES dbo.products(id) ON DELETE CASCADE
    );
END
GO

-- Create indexes for better performance
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_users_username' AND object_id = OBJECT_ID('dbo.users'))
    CREATE INDEX idx_users_username ON dbo.users(username);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_users_email' AND object_id = OBJECT_ID('dbo.users'))
    CREATE INDEX idx_users_email ON dbo.users(email);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_users_role' AND object_id = OBJECT_ID('dbo.users'))
    CREATE INDEX idx_users_role ON dbo.users(role);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_products_category' AND object_id = OBJECT_ID('dbo.products'))
    CREATE INDEX idx_products_category ON dbo.products(category_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_products_sku' AND object_id = OBJECT_ID('dbo.products'))
    CREATE INDEX idx_products_sku ON dbo.products(sku);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_products_active' AND object_id = OBJECT_ID('dbo.products'))
    CREATE INDEX idx_products_active ON dbo.products(is_active);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_orders_user' AND object_id = OBJECT_ID('dbo.orders'))
    CREATE INDEX idx_orders_user ON dbo.orders(user_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_orders_status' AND object_id = OBJECT_ID('dbo.orders'))
    CREATE INDEX idx_orders_status ON dbo.orders(status);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_orders_created' AND object_id = OBJECT_ID('dbo.orders'))
    CREATE INDEX idx_orders_created ON dbo.orders(created_at);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_order_items_order' AND object_id = OBJECT_ID('dbo.order_items'))
    CREATE INDEX idx_order_items_order ON dbo.order_items(order_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_order_items_product' AND object_id = OBJECT_ID('dbo.order_items'))
    CREATE INDEX idx_order_items_product ON dbo.order_items(product_id);
GO

-- Insert default categories (use MERGE to emulate upsert behavior)
MERGE dbo.categories AS target
USING (VALUES
    (N'Kitchen Supplies', N'Kitchen and cooking equipment for hotels'),
    (N'Cleaning Supplies', N'Cleaning and maintenance products'),
    (N'Bedding & Linens', N'Bed sheets, towels, and other linens'),
    (N'Bathroom Supplies', N'Bathroom amenities and supplies'),
    (N'Office Supplies', N'Office equipment and stationery'),
    (N'Maintenance Tools', N'Tools and equipment for hotel maintenance')
) AS src(name, description)
ON target.name = src.name
WHEN NOT MATCHED THEN
    INSERT (name, description) VALUES (src.name, src.description)
WHEN MATCHED THEN
    UPDATE SET description = src.description;
GO

-- Insert default admin user and owner if not exists
IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = N'admin')
BEGIN
    INSERT INTO dbo.users (username, email, password, full_name, role)
    VALUES (N'admin', N'admin@novahotelsupplies.com', N'$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', N'System Administrator', N'ADMIN');
END
GO

IF NOT EXISTS (SELECT 1 FROM dbo.users WHERE username = N'owner')
BEGIN
    INSERT INTO dbo.users (username, email, password, full_name, role)
    VALUES (N'owner', N'owner@novahotelsupplies.com', N'$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', N'Hotel Owner', N'OWNER');
END
GO

