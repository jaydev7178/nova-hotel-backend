package com.novahotel.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.novahotel.entity.Product;
import com.novahotel.entity.ProductImage;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProduct(Product product);
    List<ProductImage> findByProductId(Long productId);
    Optional<ProductImage> findByProductIdAndIsCoverTrue(Long productId);
    Optional<ProductImage> findByUrl(String url);
    void deleteByUrl(String url);
}
