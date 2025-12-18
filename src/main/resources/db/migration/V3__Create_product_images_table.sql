USE nova_hotel_db;
GO

IF OBJECT_ID(N'dbo.product_images', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.product_images (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        product_id BIGINT NOT NULL,
        url NVARCHAR(1000),
        file_path NVARCHAR(1000),
        is_cover BIT DEFAULT 0,
        created_at DATETIME2 DEFAULT GETDATE(),
        CONSTRAINT fk_product_images_product FOREIGN KEY (product_id) REFERENCES dbo.products(id) ON DELETE CASCADE
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'idx_product_images_product' AND object_id = OBJECT_ID('dbo.product_images'))
    CREATE INDEX idx_product_images_product ON dbo.product_images(product_id);
GO
