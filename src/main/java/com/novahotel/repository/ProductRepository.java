package com.novahotel.repository;

import com.novahotel.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySku(String sku);
    
    boolean existsBySku(String sku);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.name ASC")
    Page<Product> findAllActiveProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.isActive = true ORDER BY p.name ASC")
    Page<Product> findByCategoryIdAndIsActiveTrue(@Param("categoryId") Long categoryId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name% AND p.isActive = true ORDER BY p.name ASC")
    Page<Product> findByNameContainingIgnoreCaseAndIsActiveTrue(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.name LIKE %:name% AND p.isActive = true ORDER BY p.name ASC")
    Page<Product> findByCategoryIdAndNameContainingIgnoreCaseAndIsActiveTrue(
            @Param("categoryId") Long categoryId, 
            @Param("name") String name, 
            Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.isActive = true ORDER BY p.name ASC")
    Page<Product> findInStockProducts(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.isActive = true ORDER BY p.stockQuantity ASC")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);
    
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.isActive = true ORDER BY p.price ASC")
    Page<Product> findByPriceBetweenAndIsActiveTrue(
            @Param("minPrice") java.math.BigDecimal minPrice, 
            @Param("maxPrice") java.math.BigDecimal maxPrice, 
            Pageable pageable);
}

