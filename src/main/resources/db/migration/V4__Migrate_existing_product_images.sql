USE nova_hotel_db;
GO

-- Migrate cover image from products.image_url into product_images
IF EXISTS (SELECT 1 FROM dbo.products WHERE image_url IS NOT NULL AND LTRIM(RTRIM(image_url)) <> '')
BEGIN
    INSERT INTO dbo.product_images (product_id, url, file_path, is_cover, created_at)
    SELECT id, image_url, NULL, 1, GETDATE()
    FROM dbo.products
    WHERE image_url IS NOT NULL AND LTRIM(RTRIM(image_url)) <> '';
END
GO

-- Migrate additional images from products.image_urls
-- Supports JSON array (SQL Server OPENJSON) or CSV via STRING_SPLIT
DECLARE @count INT = (SELECT COUNT(1) FROM dbo.products WHERE image_urls IS NOT NULL AND LTRIM(RTRIM(image_urls)) <> '');
IF @count > 0
BEGIN
    -- Handle JSON arrays
    INSERT INTO dbo.product_images (product_id, url, file_path, is_cover, created_at)
    SELECT p.id, j.value, NULL, 0, GETDATE()
    FROM dbo.products p
    CROSS APPLY (
        SELECT value FROM OPENJSON(p.image_urls)
    ) j
    WHERE p.image_urls IS NOT NULL AND ISJSON(p.image_urls) = 1;

    -- Handle CSV or single-value (non-JSON)
    INSERT INTO dbo.product_images (product_id, url, file_path, is_cover, created_at)
    SELECT p.id, LTRIM(RTRIM(value)), NULL, 0, GETDATE()
    FROM dbo.products p
    CROSS APPLY STRING_SPLIT(p.image_urls, ',')
    WHERE p.image_urls IS NOT NULL AND ISJSON(p.image_urls) = 0;
END
GO

-- Optionally clear the old columns (commented out - keep for safety)
-- UPDATE dbo.products SET image_url = NULL, image_urls = NULL WHERE image_url IS NOT NULL OR image_urls IS NOT NULL;
GO
