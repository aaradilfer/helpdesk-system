package com.helpdesk.service;

import com.helpdesk.entity.Category;
import com.helpdesk.repository.CategoryRepository;
import com.helpdesk.strategy.CategoryStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Payment Portal Category Service - Adapter for existing CategoryRepository
 * Supports Strategy Pattern for flexible category validation
 */
@Service("paymentCategoryService")
@Transactional
public class PaymentCategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private CategoryStrategy categoryStrategy; // Strategy Pattern for flexible validation

    /**
     * Create category with strategy-based validation
     * Supports both Strict (no duplicates) and Lenient (allow duplicates) modes
     */
    public Category createCategory(Category category) {
        // STRATEGY PATTERN: Validate using configured strategy
        String validationError = categoryStrategy.validateCategory(category.getName());
        if (validationError != null) {
            throw new RuntimeException(validationError);
        }
        
        // Get existing category names for duplicate check
        List<String> existingNames = categoryRepository.findAll()
            .stream()
            .map(Category::getName)
            .collect(Collectors.toList());
        
        // STRATEGY PATTERN: Apply strategy-specific rules (duplicate check)
        try {
            categoryStrategy.updateCategory(null, category.getName(), existingNames);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e.getMessage());
        }
        
        System.out.println("[PaymentCategoryService] Category validated using " + 
                         categoryStrategy.getStrategyName() + " strategy");
        
        return categoryRepository.save(category);
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public List<Category> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    public Category updateCategory(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    public void toggleCategoryStatus(Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            category.setIsActive(!category.getIsActive());
            categoryRepository.save(category);
        }
    }
    
    /**
     * Get the name of the currently active category strategy
     * @return Strategy name (e.g., "Strict" or "Lenient")
     */
    public String getCurrentCategoryStrategyName() {
        return categoryStrategy.getStrategyName();
    }
}

