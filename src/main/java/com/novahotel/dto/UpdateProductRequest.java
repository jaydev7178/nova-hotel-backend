package com.novahotel.dto;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProductRequest {

    @Size(min = 2, max = 200)
    private String name;

    private String description;

    private BigDecimal price;

    @Min(0)
    private Integer stockQuantity;

    private String sku;
    private Boolean isActive;

    private Long categoryId;

    /* ---------- IMAGES ---------- */
    private MultipartFile coverImage;
    private List<MultipartFile> galleryImages;
}
