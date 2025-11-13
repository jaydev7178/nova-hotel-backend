package com.novahotel.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novahotel.dto.CheckoutRequest;
import com.novahotel.dto.CheckoutRequest.ShippingAddressDto;
import com.novahotel.dto.OrderDTO;
import com.novahotel.entity.Order;
import com.novahotel.entity.OrderItem;
import com.novahotel.entity.Product;
import com.novahotel.entity.User;
import com.novahotel.repository.OrderItemRepository;
import com.novahotel.repository.OrderRepository;

@Service
@Transactional
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderItemRepository orderItemRepository;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;
    
    @Transactional
    public Order createOrder(Long userId, List<CheckoutRequest.CartItemDto> cartItems, 
                        ShippingAddressDto shippingAddress, String notes) {
        
        // 1. Validate user
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        
        // 2. Validate cart is not empty
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }
        
        // 3. Validate shipping address
        if (shippingAddress == null) {
            throw new IllegalArgumentException("Shipping address is required");
        }
        
        // 4. Create order
        Order order = new Order();
        order.setUser(user);
        
        // Format shipping address as string
        String formattedAddress = String.format("%s, %s, %s", 
            shippingAddress.getStreet(), 
            shippingAddress.getCity(), 
            shippingAddress.getZipCode()
        );
        order.setShippingAddress(formattedAddress);
        order.setNotes(notes);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTermsAccepted(false);
        
        // 5. Process cart items and calculate total
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (CheckoutRequest.CartItemDto cartItem : cartItems) {
            Long productId = cartItem.getProductId();
            Integer quantity = cartItem.getQuantity();
            
            // Validate quantity
            if (quantity == null || quantity <= 0) {
                throw new IllegalArgumentException("Invalid quantity for product id: " + productId);
            }
            
            // Get and validate product
            Product product = productService.getProductById(productId);
            if (product == null) {
                throw new IllegalArgumentException("Product not found with id: " + productId);
            }
            
            if (!product.getIsActive()) {
                throw new IllegalArgumentException("Product is not available: " + product.getName());
            }
            
            // Check stock availability
            if (product.getStockQuantity() < quantity) {
                throw new IllegalArgumentException(
                    String.format("Insufficient stock for product: %s. Available: %d, Requested: %d",
                        product.getName(), product.getStockQuantity(), quantity)
                );
            }
            
            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());
            
            // Reduce stock quantity
            product.setStockQuantity(product.getStockQuantity() - quantity);
            productService.updateProduct(product.getId(), product);
        }
        
        // 6. Set order items and total amount
        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        
        // 7. Save order (cascade should save order items too)
        return orderRepository.save(order);
    }

    
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setStatus(status);
        return orderRepository.save(order);
    }
    
    public Order acceptTermsAndUpdateOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new RuntimeException("Order cannot be updated in current status");
        }
        
        order.setTermsAccepted(true);
        order.setStatus(Order.OrderStatus.APPROVED);
        
        // Update stock quantities
        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
        for (OrderItem item : orderItems) {
            productService.updateStock(item.getProduct().getId(), item.getQuantity());
        }
        
        return orderRepository.save(order);
    }
    
    public Order sendPaymentInfo(Long orderId, String paymentInfo) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (order.getStatus() != Order.OrderStatus.APPROVED) {
            throw new RuntimeException("Order must be approved before sending payment info");
        }
        
        order.setPaymentInfo(paymentInfo);
        order.setStatus(Order.OrderStatus.PAYMENT_INFO_SENT);
        
        // Send payment info email to user
        emailService.sendPaymentInfoEmail(order);
        
        return orderRepository.save(order);
    }
    
    public Order confirmPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (order.getStatus() != Order.OrderStatus.PAYMENT_INFO_SENT) {
            throw new RuntimeException("Payment info must be sent before confirming payment");
        }
        
        order.setStatus(Order.OrderStatus.PAYMENT_CONFIRMED);
        return orderRepository.save(order);
    }
    
    public Order initiateDelivery(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (order.getStatus() != Order.OrderStatus.PAYMENT_CONFIRMED) {
            throw new RuntimeException("Payment must be confirmed before initiating delivery");
        }
        
        order.setStatus(Order.OrderStatus.DELIVERY_INITIATED);
        
        // Send delivery notification
        emailService.sendDeliveryInitiatedEmail(order);
        
        return orderRepository.save(order);
    }
    
    public Order markAsDelivered(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if (order.getStatus() != Order.OrderStatus.DELIVERY_INITIATED) {
            throw new RuntimeException("Delivery must be initiated before marking as delivered");
        }
        
        order.setStatus(Order.OrderStatus.DELIVERED);
        return orderRepository.save(order);
    }
    
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
    
    public Order getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
    
    public Page<Order> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    public Page<OrderDTO> getOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        //return orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        Page<Order> orders = orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return orders.map(this::convertToDTO);
    }
    
    public Page<Order> getUserOrdersByStatus(Long userId, Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);  
    }
    
    public List<Order> getOrdersByStatuses(List<Order.OrderStatus> statuses) {
        return orderRepository.findByStatusInOrderByCreatedAtDesc(statuses, Pageable.unpaged()).getContent();
    }
    
    public Long getOrderCountByStatus(Order.OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    public Page<OrderDTO> getAllOrders(Pageable pageable) {
       Page<Order> orders = orderRepository.findAllOrders(pageable);
        return orders.map(this::convertToDTO);
    }

    private OrderDTO convertToDTO(Order order) {
    OrderDTO dto = new OrderDTO();
    dto.setId(order.getId());
    dto.setOrderNumber(order.getOrderNumber());
    dto.setUserId(order.getUser().getId());
    dto.setStatus(order.getStatus());
    dto.setTotalAmount(order.getTotalAmount());
    dto.setShippingAddress(order.getShippingAddress());
    dto.setNotes(order.getNotes());
    dto.setPaymentInfo(order.getPaymentInfo());
    dto.setTermsAccepted(order.getTermsAccepted());
    dto.setCreatedAt(order.getCreatedAt());
    dto.setUpdatedAt(order.getUpdatedAt());
    return dto;
}

}

