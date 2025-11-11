package com.novahotel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CheckoutRequest {
    
    @NotNull(message = "Cart items are required")
    @NotEmpty(message = "Cart cannot be empty")
    @Valid
    private List<CartItemDto> cartItems;
    
    @NotNull(message = "Shipping address is required")
    @Valid
    private ShippingAddressDto shippingAddress;
    
    private String notes;
    
    @Data
    public static class CartItemDto {
        @NotNull(message = "Product ID is required")
        private Long productId;
        
        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }
    
    @Data
    public static class ShippingAddressDto {
        @NotNull(message = "Street is required")
        private String street;
        
        @NotNull(message = "City is required")
        private String city;
        
        @NotNull(message = "Zip code is required")
        private String zipCode;
    }
}