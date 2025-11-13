package com.novahotel.controller;

import com.novahotel.dto.OrderDTO;
import com.novahotel.entity.*;
import com.novahotel.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Admin management endpoints")
@CrossOrigin(origins = "*")
public class AdminController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private OrderService orderService;
    
    // User Management
    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/users/{id}")
    @Operation(summary = "Update user")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id, @Valid @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PutMapping("/users/{id}/deactivate")
    @Operation(summary = "Deactivate user")
    public ResponseEntity<?> deactivateUser(@PathVariable("id") Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(new ApiResponse(true, "User deactivated successfully", null));
    }
    
    @PutMapping("/users/{id}/activate")
    @Operation(summary = "Activate user")
    public ResponseEntity<?> activateUser(@PathVariable("id") Long id) {
        userService.activateUser(id);
        return ResponseEntity.ok(new ApiResponse(true, "User activated successfully", null));
    }
    
    // Category Management
    @PostMapping("/categories")
    @Operation(summary = "Create new category")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        Category createdCategory = categoryService.createCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }
    
    @PutMapping("/categories/{id}")
    @Operation(summary = "Update category")
    public ResponseEntity<Category> updateCategory(@PathVariable("id") Long id, @Valid @RequestBody Category category) {
        Category updatedCategory = categoryService.updateCategory(id, category);
        return ResponseEntity.ok(updatedCategory);
    }
    
    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Delete category")
    public ResponseEntity<?> deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(new ApiResponse(true, "Category deleted successfully", null));
    }
    
    @PutMapping("/categories/{id}/deactivate")
    @Operation(summary = "Deactivate category")
    public ResponseEntity<?> deactivateCategory(@PathVariable("id") Long id) {
        categoryService.deactivateCategory(id);
        return ResponseEntity.ok(new ApiResponse(true, "Category deactivated successfully", null));
    }
    
    @PutMapping("/categories/{id}/activate")
    @Operation(summary = "Activate category")
    public ResponseEntity<?> activateCategory(@PathVariable("id") Long id) {
        categoryService.activateCategory(id);
        return ResponseEntity.ok(new ApiResponse(true, "Category activated successfully", null));
    }
    
    // Product Management
    @PostMapping("/products")
    @Operation(summary = "Create new product")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }
    
    @PutMapping("/products/{id}")
    @Operation(summary = "Update product")
    public ResponseEntity<Product> updateProduct(@PathVariable("id") Long id, @Valid @RequestBody Product product) {
        Product updatedProduct = productService.updateProduct(id, product);
        return ResponseEntity.ok(updatedProduct);
    }
    
    @PutMapping("/products/{id}/deactivate")
    @Operation(summary = "Deactivate product")
    public ResponseEntity<?> deactivateProduct(@PathVariable("id") Long id) {
        productService.deactivateProduct(id);
        return ResponseEntity.ok(new ApiResponse(true, "Product deactivated successfully", null));
    }
    
    @PutMapping("/products/{id}/activate")
    @Operation(summary = "Activate product")
    public ResponseEntity<?> activateProduct(@PathVariable("id") Long id) {
        productService.activateProduct(id);
        return ResponseEntity.ok(new ApiResponse(true, "Product activated successfully", null));
    }
    
    @PostMapping("/products/upload-image")
    @Operation(summary = "Upload product image")
    public ResponseEntity<?> uploadProductImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = productService.uploadProductImage(file);
            return ResponseEntity.ok(new ApiResponse(true, "Image uploaded successfully", imageUrl));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
    
    // Order Management
    @GetMapping("/orders")
    @Operation(summary = "Get all orders with pagination")
    public ResponseEntity<Page<OrderDTO>> getAllOrders(
            @Parameter(description = "Page number (0-based)") @RequestParam(value = "page",  defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(value = "size",  defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(value = "sortDir", defaultValue = "sortDir") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // This would need to be implemented in OrderService
        Page<OrderDTO> orders = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/orders/status/{status}")
    @Operation(summary = "Get orders by status")
    public ResponseEntity<Page<OrderDTO>> getOrdersByStatus(
            @PathVariable Order.OrderStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "page", defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<OrderDTO> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @PutMapping("/orders/{id}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable("id") Long id, @RequestBody StatusUpdateRequest request) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(id, request.getStatus());
            return ResponseEntity.ok(new ApiResponse(true, "Order status updated successfully", updatedOrder));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
    
    @GetMapping("/orders/stats")
    @Operation(summary = "Get order statistics")
    public ResponseEntity<?> getOrderStats() {
        // This would need to be implemented in OrderService
        return ResponseEntity.ok(new ApiResponse(true, "Order statistics", null));
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
    
    public static class StatusUpdateRequest {
        private Order.OrderStatus status;
        
        public Order.OrderStatus getStatus() { return status; }
        public void setStatus(Order.OrderStatus status) { this.status = status; }
    }
}

