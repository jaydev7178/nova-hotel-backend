package com.novahotel.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.novahotel.dto.CartItemDto;
import com.novahotel.dto.CheckoutRequest;
import com.novahotel.dto.OrderDTO;
import com.novahotel.dto.OrderItemDTO;
import com.novahotel.entity.Order;
import com.novahotel.entity.OrderItem;
import com.novahotel.entity.Product;
import com.novahotel.entity.User;
import com.novahotel.repository.ProductRepository;
import com.novahotel.service.OrderItemService;
import com.novahotel.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Order management endpoints")
@CrossOrigin(origins = "*")
public class OrderController {
    
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private OrderItemService orderItemService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @PostMapping("/AddToCart")
    @Operation(summary = "Add product to cart")
    public ResponseEntity<?> addToCart(
            @Valid @RequestBody OrderItemDTO request, 
            Authentication authentication) {
        log.info("Adding product {} to cart for user", request.getProductId());
        try {
            User user = (User) authentication.getPrincipal();
            log.debug("User {} adding product {} with quantity {} to cart", 
                user.getId(), request.getProductId(), request.getQuantity());
            
            // Fetch product from database
            Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            
            // Create OrderItem for cart (order is null)
            OrderItem cartItem = new OrderItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(request.getQuantity());
            cartItem.setUnitPrice(request.getUnitPrice());
            cartItem.setOrder(null); // Explicitly set order to null for cart
            
            // Save to cart using OrderItemService
            OrderItem savedItem = orderItemService.saveToCart(cartItem);
            OrderItemDTO responseDto = new OrderItemDTO();
            responseDto.setId(savedItem.getId());
            responseDto.setQuantity(savedItem.getQuantity());
            responseDto.setUnitPrice(savedItem.getUnitPrice());
            responseDto.setTotalPrice(savedItem.getTotalPrice());
            responseDto.setProductId(savedItem.getProduct().getId());
            responseDto.setUserId(savedItem.getUser().getId());
            responseDto.setCreatedAt(savedItem.getCreatedAt());
            responseDto.setUpdatedAt(savedItem.getUpdatedAt());
            
            log.info("Successfully added product {} to cart for user {}", 
                request.getProductId(), user.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Product added to cart successfully", responseDto));
                
        } catch (IllegalArgumentException e) {
            log.warn("Failed to add product to cart: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, e.getMessage(), null));
                
        } catch (Exception e) {
            log.error("Unexpected error adding product to cart", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "An error occurred while adding product to cart", null));
        }
    }
    
    @GetMapping("/GetCartItem")
    @Operation(summary = "Get all cart items for authenticated user")
    public ResponseEntity<?> getCartItems(Authentication authentication) {
        log.info("Retrieving cart items for user");
        try {
            User user = (User) authentication.getPrincipal();
            log.debug("Getting cart items for user {}", user.getId());
            
            // Get cart items where orderId is null
            List<OrderItemDTO> cartItems = orderItemService.getCartItems(user.getId());
            
            log.info("Successfully retrieved {} cart items for user {}", 
                cartItems.size(), user.getId());
            return ResponseEntity.ok(new ApiResponse(true, "Cart items retrieved successfully", cartItems));
                
        } catch (Exception e) {
            log.error("Error retrieving cart items", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "An error occurred while retrieving cart items", null));
        }
    }
    
    @PostMapping("/RemoveFromCart")
    @Operation(summary = "Remove product from cart")
    public ResponseEntity<?> removeFromCart(
            @RequestParam Long productId,
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            
            // Remove from cart where orderId is null
            orderItemService.removeFromCart(user.getId(), productId);
            
            return ResponseEntity.ok(new ApiResponse(true, "Product removed from cart successfully", null));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, e.getMessage(), null));
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "An error occurred while removing product from cart", null));
        }
    }
    
    @PostMapping("/checkout")
    @Operation(summary = "Create new order from cart")
    public ResponseEntity<?> checkout(
            @Valid @RequestBody CheckoutRequest request, 
            Authentication authentication) {
        log.info("Processing checkout request");
        try {
            User user = (User) authentication.getPrincipal();
            
            // Automatically fetch cart items using the same logic as GetCartItem
            List<OrderItemDTO> orderItemDTOs = orderItemService.getCartItems(user.getId());
            
            if (orderItemDTOs.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Your cart is empty. Please add items to cart before checkout.", null));
            }
            
            // Convert OrderItemDTO to CartItemDto
            List<CartItemDto> cartItems = orderItemDTOs.stream()
                .map(item -> {
                    CartItemDto dto = new CartItemDto();
                    dto.setProductId(item.getProductId());
                    dto.setQuantity(item.getQuantity());
                    return dto;
                })
                .toList();
            
            log.debug("Processing checkout for user {} with {} items", 
                user.getId(), cartItems.size());
            
            Order order = orderService.createOrder(
                user.getId(), 
                cartItems, 
                request.getShippingAddress(), 
                request.getNotes()
            );
            
            log.info("Successfully created order {} for user {}", order.getId(), user.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Order created successfully", order));
                
        } catch (IllegalArgumentException e) {
            log.warn("Checkout validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, e.getMessage(), null));
                
        } catch (Exception e) {
            log.error("Unexpected error during checkout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "An error occurred while processing your order", null));
        }
    }

    @GetMapping
    @Operation(summary = "Get user orders with pagination")
    public ResponseEntity<Page<Order>> getUserOrders(
            Authentication authentication,
            @Parameter(description = "Page number (0-based)") @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "Sort by field") @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {
        
        User user = (User) authentication.getPrincipal();
        log.info("Retrieving orders for user {} - page: {}, size: {}", user.getId(), page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Order> orders = orderService.getUserOrders(user.getId(), pageable);
        log.debug("Retrieved {} orders for user {}", orders.getTotalElements(), user.getId());
        
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Order order = orderService.getOrderById(id);
        
        // Check if user owns this order or is admin/owner
        if (!order.getUser().getId().equals(user.getId()) && 
            !user.getRole().equals(User.Role.OWNER) && 
            !user.getRole().equals(User.Role.ADMIN)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/order-number/{orderNumber}")
    @Operation(summary = "Get order by order number")
    public ResponseEntity<Order> getOrderByOrderNumber(@PathVariable String orderNumber) {
        Order order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(order);
    }
    
    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status")
    public ResponseEntity<Page<OrderDTO>> getOrdersByStatus(
            @PathVariable Order.OrderStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<OrderDTO> orders = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/user/{userId}/status/{status}")
    @Operation(summary = "Get user orders by status")
    public ResponseEntity<Page<Order>> getUserOrdersByStatus(
            @PathVariable Long userId,
            @PathVariable Order.OrderStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Order> orders = orderService.getUserOrdersByStatus(userId, status, pageable);
        return ResponseEntity.ok(orders);
    }
    
    @PutMapping("/{id}/accept-terms")
    @Operation(summary = "Accept terms and update order status")
    public ResponseEntity<?> acceptTermsAndUpdateOrder(@PathVariable Long id, Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            Order order = orderService.getOrderById(id);
            
            // Check if user owns this order
            if (!order.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Access denied", null));
            }
            
            Order updatedOrder = orderService.acceptTermsAndUpdateOrder(id);
            return ResponseEntity.ok(new ApiResponse(true, "Terms accepted and order updated", updatedOrder));
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "An error occurred while updating order", null));
        }
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
    
    public static class PaymentInfoRequest {
        private String paymentInfo;
        
        public String getPaymentInfo() { return paymentInfo; }
        public void setPaymentInfo(String paymentInfo) { this.paymentInfo = paymentInfo; }
    }
}