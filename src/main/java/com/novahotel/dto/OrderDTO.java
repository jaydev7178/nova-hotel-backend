package com.novahotel.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.novahotel.entity.Order.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private String orderNumber;
    private Long userId;  // Only the ID, not the whole User object
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String notes;
    private String paymentInfo;
    private Boolean termsAccepted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemDTO> orderItems;
    // Don't include orderItems unless needed
}

