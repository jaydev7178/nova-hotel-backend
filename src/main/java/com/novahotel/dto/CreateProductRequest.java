package com.novahotel.dto;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateProductRequest {

    @NotBlank
    @Size(min = 2, max = 200)
    private String name;

    private String description;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @NotNull
    @Min(0)
    private Integer stockQuantity;

    private String sku;
    private Boolean isActive = true;

    @NotNull(message = "Category is required")
    private Long categoryId;

    /* ---------- IMAGES ---------- */
    private MultipartFile coverImage;
    private List<MultipartFile> galleryImages;
}
