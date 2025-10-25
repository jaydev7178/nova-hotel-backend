package com.novahotel.service;

import com.novahotel.entity.Category;
import com.novahotel.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Category with this name already exists");
        }
        category.setIsActive(true);
        return categoryRepository.save(category);
    }
    
    public Category updateCategory(Long categoryId, Category updatedCategory) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        if (updatedCategory.getName() != null && !updatedCategory.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(updatedCategory.getName())) {
                throw new RuntimeException("Category with this name already exists");
            }
            category.setName(updatedCategory.getName());
        }
        
        if (updatedCategory.getDescription() != null) {
            category.setDescription(updatedCategory.getDescription());
        }
        
        return categoryRepository.save(category);
    }
    
    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }
    
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    public List<Category> getActiveCategories() {
        return categoryRepository.findAllActiveCategories();
    }
    
    public List<Category> searchCategoriesByName(String name) {
        return categoryRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name);
    }
    
    public void deactivateCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);
        category.setIsActive(false);
        categoryRepository.save(category);
    }
    
    public void activateCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);
        category.setIsActive(true);
        categoryRepository.save(category);
    }
    
    public void deleteCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new RuntimeException("Cannot delete category with existing products");
        }
        categoryRepository.delete(category);
    }
}

