package com.helpdesk.strategy;

import com.helpdesk.entity.Category;
import java.util.List;

/**
 * Strategy interface for category validation
 * Supports different validation approaches (Strict, Lenient)
 */
public interface CategoryStrategy {
    /**
     * Validate category name
     * @param categoryName The category name to validate
     * @return Error message if invalid, null if valid
     */
    String validateCategory(String categoryName);
    
    /**
     * Validate category entity
     * @param category The category to validate
     * @return true if valid, false otherwise
     */
    default boolean validateCategory(Category category) {
        if (category == null) {
            return false;
        }
        String error = validateCategory(category.getName());
        return error == null;
    }
    
    /**
     * Check and update category based on strategy rules
     * @param oldName The old category name (null for new category)
     * @param newName The new category name
     * @param existingNames List of existing category names
     * @throws IllegalArgumentException if update violates strategy rules
     */
    void updateCategory(String oldName, String newName, List<String> existingNames);
    
    /**
     * Get the name of the strategy
     * @return Strategy name
     */
    String getStrategyName();
}
