package com.novahotel.service;

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

        if (item.getProduct() != null) {
            dto.setProductId(item.getProduct().getId());
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

}
