package com.helpdesk.strategy;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Lenient category validation strategy
 * Allows duplicate category names
 */
@Component("lenientCategoryStrategy")
public class LenientCategoryStrategy implements CategoryStrategy {
    
    @Override
    public String validateCategory(String categoryName) {
        // Only check if not empty
        return (categoryName == null || categoryName.trim().isEmpty()) 
            ? "Category name cannot be empty" 
            : null;
    }
    
    @Override
    public void updateCategory(String oldName, String newName, List<String> existingNames) {
        // Lenient mode allows duplicates - no validation needed
    }
    
    @Override
    public String getStrategyName() {
        return "Lenient";
    }
}
