package com.novahotel.controller;

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

import com.novahotel.dto.CheckoutRequest;
import com.novahotel.dto.OrderDTO;
import com.novahotel.entity.Order;
import com.novahotel.entity.User;
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
    
    @Autowired
    private OrderService orderService;
    
    @PostMapping("/checkout")
    @Operation(summary = "Create new order from cart")
    public ResponseEntity<?> checkout(
            @Valid @RequestBody CheckoutRequest request, 
            Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            
            Order order = orderService.createOrder(
                user.getId(), 
                request.getCartItems(), 
                request.getShippingAddress(), 
                request.getNotes()
            );
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Order created successfully", order));
                
        } catch (IllegalArgumentException e) {
            // Handle validation errors
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, e.getMessage(), null));
                
        } catch (Exception e) {
            // Log unexpected errors
            // log.error("Unexpected error during checkout for user {}", user.getId(), e);
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
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Order> orders = orderService.getUserOrders(user.getId(), pageable);
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
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, "Access denied", null));
            }
            
            Order updatedOrder = orderService.acceptTermsAndUpdateOrder(id);
            return ResponseEntity.ok(new ApiResponse(true, "Terms accepted and order approved", updatedOrder));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
    
    @PutMapping("/{id}/send-payment-info")
    @Operation(summary = "Send payment information to user")
    public ResponseEntity<?> sendPaymentInfo(@PathVariable Long id, @RequestBody PaymentInfoRequest request) {
        try {
            Order updatedOrder = orderService.sendPaymentInfo(id, request.getPaymentInfo());
            return ResponseEntity.ok(new ApiResponse(true, "Payment information sent", updatedOrder));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
    
    @PutMapping("/{id}/confirm-payment")
    @Operation(summary = "Confirm payment received")
    public ResponseEntity<?> confirmPayment(@PathVariable Long id) {
        try {
            Order updatedOrder = orderService.confirmPayment(id);
            return ResponseEntity.ok(new ApiResponse(true, "Payment confirmed", updatedOrder));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
    
    @PutMapping("/{id}/initiate-delivery")
    @Operation(summary = "Initiate order delivery")
    public ResponseEntity<?> initiateDelivery(@PathVariable Long id) {
        try {
            Order updatedOrder = orderService.initiateDelivery(id);
            return ResponseEntity.ok(new ApiResponse(true, "Delivery initiated", updatedOrder));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
        }
    }
    
    @PutMapping("/{id}/mark-delivered")
    @Operation(summary = "Mark order as delivered")
    public ResponseEntity<?> markAsDelivered(@PathVariable Long id) {
        try {
            Order updatedOrder = orderService.markAsDelivered(id);
            return ResponseEntity.ok(new ApiResponse(true, "Order marked as delivered", updatedOrder));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage(), null));
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

