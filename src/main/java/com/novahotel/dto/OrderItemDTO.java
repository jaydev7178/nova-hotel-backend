package com.novahotel.dto;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrderItemDTO {
    private Long id;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Long productId;
    private Long userId;
    private Long orderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}