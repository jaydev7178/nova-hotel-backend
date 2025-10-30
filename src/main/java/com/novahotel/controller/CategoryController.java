package com.novahotel.controller;

import com.novahotel.entity.Category;
import com.novahotel.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@Tag(name = "Categories", description = "Category management endpoints")
@CrossOrigin(origins = "*")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    @GetMapping
    @Operation(summary = "Get all active categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getActiveCategories();
        return ResponseEntity.ok(categories);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
    
    @GetMapping("/search")
    @Operation(summary = "Search categories by name")
    public ResponseEntity<List<Category>> searchCategories(@RequestParam String name) {
        List<Category> categories = categoryService.searchCategoriesByName(name);
        return ResponseEntity.ok(categories);
    }
}

