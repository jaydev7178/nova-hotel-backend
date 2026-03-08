package com.novahotel.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novahotel.dto.OrderItemDTO;
import com.novahotel.entity.Order;
import com.novahotel.entity.OrderItem;
import com.novahotel.entity.Product;
import com.novahotel.repository.OrderItemRepository;

import lombok.RequiredArgsConstructor;


@Service
@Transactional
@RequiredArgsConstructor
public class OrderItemService {
	
    private final OrderItemRepository orderItemRepository;


    private OrderItemDTO mapToDto(OrderItem item) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(item.getId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTotalPrice(item.getTotalPrice());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        dto.setUserId(item.getUser().getId());
        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
        }
        if (item.getOrder() != null) {
            dto.setOrderId(item.getOrder().getId());
        }

        return dto;
    }


    private List<OrderItemDTO> mapToDtoList(List<OrderItem> items) {
        return items.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public OrderItem save(OrderItem orderItem) {
        return orderItemRepository.save(orderItem);
    }

    public Optional<OrderItemDTO> findById(Long id) {
        return orderItemRepository.findById(id).map(this::mapToDto);
    }

    public List<OrderItemDTO> findAll() {
        return mapToDtoList(orderItemRepository.findAll());
    }

    public void deleteById(Long id) {
        orderItemRepository.deleteById(id);
    }

    public List<OrderItemDTO> findByOrder(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        return mapToDtoList(items);
    }

    public List<OrderItemDTO> findByProduct(Product product) {
        List<OrderItem> items = orderItemRepository.findByProduct(product);
        return mapToDtoList(items);
    }

    public List<OrderItemDTO> findByOrderId(Long orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return mapToDtoList(items);
    }

    public List<OrderItemDTO> findByProductId(Long productId) {
        List<OrderItem> items = orderItemRepository.findByProductId(productId);
        return mapToDtoList(items);
    }

    public Long getTotalQuantitySold(Long productId, List<Order.OrderStatus> statuses) {
        return orderItemRepository.getTotalQuantitySoldByProductIdAndStatusIn(productId, statuses);
    }

    // Cart Operations
    /**
     * Save OrderItem to cart (order will be null)
     * @param orderItem the order item to save
     * @return saved OrderItem
     */
    public OrderItem saveToCart(OrderItem orderItem) {
        // Validate required fields
        if (orderItem.getUser() == null) {
            throw new IllegalArgumentException("User is required");
        }
        if (orderItem.getProduct() == null) {
            throw new IllegalArgumentException("Product is required");
        }
        if (orderItem.getQuantity() == null || orderItem.getQuantity() < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        if (orderItem.getUnitPrice() == null) {
            throw new IllegalArgumentException("Unit price is required");
        }
        
        // Calculate total price
        orderItem.setTotalPrice(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        
        // Ensure order is null for cart items
        orderItem.setOrder(null);
        
        return orderItemRepository.save(orderItem);
    }

    /**
     * Get all cart items for a user (where order is null)
     * @param userId the user ID
     * @return list of cart items as DTOs
     */
    public List<OrderItemDTO> getCartItems(Long userId) {
        List<OrderItem> cartItems = orderItemRepository.findCartItemsByUserId(userId);
        return mapToDtoList(cartItems);
    }

    /**
     * Remove a product from user's cart
     * @param userId the user ID
     * @param productId the product ID
     */
    public void removeFromCart(Long userId, Long productId) {
        List<OrderItem> cartItems = orderItemRepository.findCartItemByUserIdAndProductId(userId, productId);
        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("Cart item not found");
        }
        // Delete all cart items matching the criteria
        for (OrderItem item : cartItems) {
            orderItemRepository.delete(item);
        }
    }

    /**
     * Get cart item count for a user
     * @param userId the user ID
     * @return count of items in cart
     */
    public long getCartItemCount(Long userId) {
        return orderItemRepository.findCartItemsByUserId(userId).size();
    }

    /**
     * Clear all cart items for a user
     * @param userId the user ID
     */
    public void clearCart(Long userId) {
        List<OrderItem> cartItems = orderItemRepository.findCartItemsByUserId(userId);
        orderItemRepository.deleteAll(cartItems);
    }
}