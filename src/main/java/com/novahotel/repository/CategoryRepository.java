package com.novahotel.repository;

import com.novahotel.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.name ASC")
    List<Category> findAllActiveCategories();
    
    @Query("SELECT c FROM Category c WHERE c.name LIKE %:name% AND c.isActive = true")
    List<Category> findByNameContainingIgnoreCaseAndIsActiveTrue(@Param("name") String name);
}

