-- Insert sample products
INSERT INTO products (name, description, price, stock_quantity, sku, category_id) VALUES
-- Kitchen Supplies
('Professional Chef Knife Set', 'High-quality stainless steel knife set for professional kitchens', 89.99, 50, 'KIT-001', 1),
('Commercial Coffee Maker', 'Large capacity coffee maker for hotel breakfast service', 299.99, 25, 'KIT-002', 1),
('Stainless Steel Mixing Bowls', 'Set of 5 mixing bowls in various sizes', 45.99, 100, 'KIT-003', 1),
('Commercial Blender', 'Heavy-duty blender for smoothies and food preparation', 199.99, 30, 'KIT-004', 1),

-- Cleaning Supplies
('Multi-Purpose Cleaner', 'Eco-friendly all-purpose cleaner concentrate', 12.99, 200, 'CLE-001', 2),
('Microfiber Cleaning Cloths', 'Pack of 24 high-quality microfiber cloths', 18.99, 150, 'CLE-002', 2),
('Commercial Vacuum Cleaner', 'Heavy-duty vacuum for hotel housekeeping', 399.99, 15, 'CLE-003', 2),
('Disinfectant Spray', 'Hospital-grade disinfectant spray', 8.99, 300, 'CLE-004', 2),

-- Bedding & Linens
('Premium Cotton Sheets', '100% cotton bed sheets, set of 2', 49.99, 100, 'BED-001', 3),
('Bath Towel Set', 'Set of 4 premium bath towels', 39.99, 80, 'BED-002', 3),
('Pillow Cases', 'Pack of 4 white pillow cases', 24.99, 120, 'BED-003', 3),
('Duvet Cover Set', 'Luxury duvet cover with matching pillow cases', 79.99, 60, 'BED-004', 3),

-- Bathroom Supplies
('Shampoo Dispensers', 'Wall-mounted shampoo dispensers', 29.99, 50, 'BAT-001', 4),
('Soap Dispensers', 'Automatic soap dispensers', 19.99, 75, 'BAT-002', 4),
('Bathroom Mirrors', 'Framed bathroom mirrors, various sizes', 89.99, 40, 'BAT-003', 4),
('Towel Racks', 'Stainless steel towel racks', 34.99, 90, 'BAT-004', 4),

-- Office Supplies
('Desk Organizer Set', 'Professional desk organizer with compartments', 25.99, 100, 'OFF-001', 5),
('Whiteboard Set', 'Magnetic whiteboard with markers', 45.99, 50, 'OFF-002', 5),
('Filing Cabinet', '2-drawer filing cabinet', 89.99, 30, 'OFF-003', 5),
('Office Chairs', 'Ergonomic office chairs', 149.99, 25, 'OFF-004', 5),

-- Maintenance Tools
('Tool Kit Set', 'Complete tool kit for hotel maintenance', 79.99, 40, 'MAI-001', 6),
('Ladder Set', 'Multi-purpose aluminum ladders', 129.99, 20, 'MAI-002', 6),
('Power Drill Set', 'Cordless power drill with accessories', 99.99, 35, 'MAI-003', 6),
('Safety Equipment', 'Safety goggles, gloves, and hard hats', 29.99, 100, 'MAI-004', 6)
ON DUPLICATE KEY UPDATE name = VALUES(name);

