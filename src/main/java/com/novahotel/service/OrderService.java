package com.novahotel.service;

import com.novahotel.entity.*;
import com.novahotel.repository.OrderRepository;
import com.novahotel.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    
    public Order createOrder(Long userId, Map<Long, Integer> cartItems, String shippingAddress, String notes) {
        User user = userService.getUserById(userId);
        
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setNotes(notes);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTermsAccepted(false);
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (Map.Entry<Long, Integer> entry : cartItems.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();
            
            Product product = productService.getProductById(productId);
            
            if (product.getStockQuantity() < quantity) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            
            totalAmount = totalAmount.add(orderItem.getTotalPrice());
        }
        
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        
        // Save order items
        for (Map.Entry<Long, Integer> entry : cartItems.entrySet()) {
            Long productId = entry.getKey();
            Integer quantity = entry.getValue();
            
            Product product = productService.getProductById(productId);
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
            
            orderItemRepository.save(orderItem);
        }
        
        // Send confirmation emails
        emailService.sendOrderConfirmationEmail(savedOrder);
        emailService.sendOrderNotificationToOwner(savedOrder);
        
        return savedOrder;
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
    
    public Page<Order> getOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
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
}

