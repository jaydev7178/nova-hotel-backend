package com.novahotel.controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.novahotel.entity.Product;
import com.novahotel.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/products")
@Tag(name = "Products", description = "Product management endpoints")
@CrossOrigin(origins = "*")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    @Operation(summary = "Get all products with pagination")
    public ResponseEntity<Page<Product>> getAllProducts(
            @Parameter(description = "Page number (0-based)") 
            @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size") 
            @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "Sort by field") 
            @RequestParam(name = "sortBy", defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction") 
            @RequestParam(name = "sortDir", defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<Product> getProductById(@PathVariable("id") Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get products by category")
    public ResponseEntity<Page<Product>> getProductsByCategory(
            @PathVariable(value = "categoryId") Long categoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> products = productService.getProductsByCategory(categoryId, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search products by name")
    public ResponseEntity<Page<Product>> searchProducts(
            @RequestParam String name,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> products = productService.searchProducts(name, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/search/category/{categoryId}")
    @Operation(summary = "Search products by name within category")
    public ResponseEntity<Page<Product>> searchProductsByCategory(
            @PathVariable("categoryId") Long categoryId,
            @RequestParam String name,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> products = productService.searchProductsByCategory(categoryId, name, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/in-stock")
    @Operation(summary = "Get products in stock")
    public ResponseEntity<Page<Product>> getInStockProducts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> products = productService.getInStockProducts(pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/price-range")
    @Operation(summary = "Get products by price range")
    public ResponseEntity<Page<Product>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Product> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(products);
    }
    
    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock products")
    public ResponseEntity<List<Product>> getLowStockProducts(
            @RequestParam(defaultValue = "10") Integer threshold) {
        
        List<Product> products = productService.getLowStockProducts(threshold);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/{id}/images/cover")
    @Operation(summary = "Upload/replace product cover image")
    public ResponseEntity<?> uploadCoverImage(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {
        try {
            Product p = productService.attachCoverImage(id, file);
            return ResponseEntity.ok(new ApiResponse(true, "Cover image updated", p.getImageUrl()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // @PostMapping("/{id}/images")
    // @Operation(summary = "Add additional images to a product")
    // public ResponseEntity<?> addAdditionalImages(
    //         @PathVariable("id") Long id,
    //         @RequestParam("files") MultipartFile[] files) {
    //     try {
    //         Product p = productService.addAdditionalImages(id, Arrays.asList(files));
    //         return ResponseEntity.ok(
    //                 new ApiResponse(true, "Images added", p.getImageUrls())
    //         );
    //     } catch (Exception e) {
    //         return ResponseEntity.badRequest()
    //                 .body(new ApiResponse(false, e.getMessage(), null));
    //     }
    // }

    @GetMapping("/{id}/images/gallery")
    @Operation(summary = "Get product images gallery (cover + additional)")
    public ResponseEntity<?> getProductGallery(@PathVariable("id") Long id) {
        try {
            Product p = productService.getProductById(id);
            List<String> additional = productService.getAdditionalImages(id);
            return ResponseEntity.ok(new ApiResponse(true, "Gallery fetched", new GalleryResponse(p.getImageUrl(), additional)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}/images")
    @Operation(summary = "Delete a specific image (cover or additional)")
    public ResponseEntity<?> deleteImage(@PathVariable("id") Long id, @RequestParam("imageUrl") String imageUrl) {
        try {
            Product p = productService.removeImage(id, imageUrl);
            return ResponseEntity.ok(new ApiResponse(true, "Image removed", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}/images")
    @Operation(summary = "Replace additional images: keep some URLs and upload new files")
    public ResponseEntity<?> replaceAdditionalImages(
            @PathVariable("id") Long id,
            @RequestParam(value = "keepUrls", required = false) String keepUrls,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {

        try {
            List<String> keep = keepUrls == null || keepUrls.trim().isEmpty()
                    ? List.of()
                    : Arrays.stream(keepUrls.split(","))
                            .map(String::trim)
                            .collect(Collectors.toList());

            // Remove images not in keep list
            List<String> existing = productService.getAdditionalImages(id);
            for (String ex : existing) {
                if (!keep.contains(ex)) {
                    productService.removeImage(id, ex);
                }
            }

            // Add new images
            if (files != null && files.length > 0) {
                productService.addAdditionalImages(id, Arrays.asList(files));
            }

            // Always fetch from DB (product_images)
            List<String> updatedImages = productService.getAdditionalImages(id);

            return ResponseEntity.ok(
                    new ApiResponse(true, "Additional images updated", updatedImages)
            );

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    // small DTO for gallery
    public static class GalleryResponse {
        private String cover;
        private List<String> images;

        public GalleryResponse(String cover, List<String> images) {
            this.cover = cover;
            this.images = images;
        }

        public String getCover() { return cover; }
        public void setCover(String cover) { this.cover = cover; }
        public List<String> getImages() { return images; }
        public void setImages(List<String> images) { this.images = images; }
    }
    
    // DTOs
    public static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;
        
        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }
        
        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
}

