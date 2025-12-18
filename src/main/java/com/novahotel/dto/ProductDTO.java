package com.novahotel.dto;

import java.math.BigDecimal;

import com.novahotel.entity.Category;
import com.novahotel.entity.Product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    @NotBlank @Size(min = 2, max = 200)
    private String name;

    private String description;

    @NotNull @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @NotNull @Min(0)
    private Integer stockQuantity;

    private String sku;
    private String imageUrl;
    private Boolean isActive = true;

    @NotNull(message = "Category is required")
    private Long categoryId;

    // getters / setters

    /* ===============================
     * ENTITY → DTO
     * =============================== */
    public static ProductDTO fromEntity(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setSku(product.getSku());
        dto.setImageUrl(product.getImageUrl());
        dto.setIsActive(product.getIsActive());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
        }

        return dto;
    }

    /* ===============================
     * DTO → ENTITY (Category injected)
     * =============================== */
    public Product toEntity(Category category) {
        Product p = new Product();
        p.setName(this.name);
        p.setDescription(this.description);
        p.setPrice(this.price);
        p.setStockQuantity(this.stockQuantity);
        p.setSku(this.sku);
        p.setImageUrl(this.imageUrl);
        p.setIsActive(this.isActive == null ? true : this.isActive);
        p.setCategory(category);
        return p;
    }
}
