package com.novahotel.service;

import com.novahotel.entity.Category;
import com.novahotel.entity.Product;
import com.novahotel.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryService categoryService;
    
    private static final String UPLOAD_DIR = "uploads/products/";
    
    public Product createProduct(Product product) {
        if (product.getSku() != null && productRepository.existsBySku(product.getSku())) {
            throw new RuntimeException("Product with this SKU already exists");
        }
        
        if (product.getSku() == null || product.getSku().isEmpty()) {
            product.setSku(generateSku());
        }
        
        product.setIsActive(true);
        return productRepository.save(product);
    }
    
    public Product updateProduct(Long productId, Product updatedProduct) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        if (updatedProduct.getName() != null) {
            product.setName(updatedProduct.getName());
        }
        if (updatedProduct.getDescription() != null) {
            product.setDescription(updatedProduct.getDescription());
        }
        if (updatedProduct.getPrice() != null) {
            product.setPrice(updatedProduct.getPrice());
        }
        if (updatedProduct.getStockQuantity() != null) {
            product.setStockQuantity(updatedProduct.getStockQuantity());
        }
        if (updatedProduct.getCategory() != null) {
            product.setCategory(updatedProduct.getCategory());
        }
        
        return productRepository.save(product);
    }
    
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
    
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAllActiveProducts(pageable);
    }
    
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId, pageable);
    }
    
    public Page<Product> searchProducts(String name, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name, pageable);
    }
    
    public Page<Product> searchProductsByCategory(Long categoryId, String name, Pageable pageable) {
        return productRepository.findByCategoryIdAndNameContainingIgnoreCaseAndIsActiveTrue(categoryId, name, pageable);
    }
    
    public Page<Product> getInStockProducts(Pageable pageable) {
        return productRepository.findInStockProducts(pageable);
    }
    
    public Page<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByPriceBetweenAndIsActiveTrue(minPrice, maxPrice, pageable);
    }
    
    public List<Product> getLowStockProducts(Integer threshold) {
        return productRepository.findLowStockProducts(threshold);
    }
    
    public void updateStock(Long productId, Integer quantity) {
        Product product = getProductById(productId);
        int newStock = product.getStockQuantity() - quantity;
        if (newStock < 0) {
            throw new RuntimeException("Insufficient stock");
        }
        product.setStockQuantity(newStock);
        productRepository.save(product);
    }
    
    public void deactivateProduct(Long productId) {
        Product product = getProductById(productId);
        product.setIsActive(false);
        productRepository.save(product);
    }
    
    public void activateProduct(Long productId) {
        Product product = getProductById(productId);
        product.setIsActive(true);
        productRepository.save(product);
    }
    
    public String uploadProductImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String filename = UUID.randomUUID().toString() + extension;
        
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath);
        
        return "/uploads/products/" + filename;
    }
    
    private String generateSku() {
        return "SKU-" + System.currentTimeMillis();
    }
}

