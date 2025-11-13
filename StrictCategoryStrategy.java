package com.helpdesk.strategy;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Strict category validation strategy
 * Does not allow duplicate category names
 */
@Component("strictCategoryStrategy")
public class StrictCategoryStrategy implements CategoryStrategy {
    
    private static final int MAX_LENGTH = 50;
    
    @Override
    public String validateCategory(String categoryName) {
        System.out.println("  ┌" + "─".repeat(76) + "┐");
        System.out.println("  │ 🔒 STRICT CATEGORY STRATEGY - Validation Process" + " ".repeat(26) + "│");
        System.out.println("  ├" + "─".repeat(76) + "┤");
        System.out.println("  │ Category: " + String.format("%-64s", categoryName != null ? categoryName : "NULL") + "│");
        
        if (categoryName == null || categoryName.trim().isEmpty()) {
            System.out.println("  │ ❌ Validation: FAILED" + " ".repeat(53) + "│");
            System.out.println("  │ 📝 Reason: Category name cannot be empty" + " ".repeat(34) + "│");
            System.out.println("  └" + "─".repeat(76) + "┘");
            return "Category name cannot be empty";
        }
        if (categoryName.length() > MAX_LENGTH) {
            System.out.println("  │ ❌ Validation: FAILED" + " ".repeat(53) + "│");
            System.out.println("  │ 📝 Reason: Name too long (max " + MAX_LENGTH + " chars, got " + categoryName.length() + ")" + " ".repeat(24) + "│");
            System.out.println("  └" + "─".repeat(76) + "┘");
            return "Category name too long (maximum " + MAX_LENGTH + " characters)";
        }
        
        System.out.println("  │ ✅ Validation: PASSED" + " ".repeat(53) + "│");
        System.out.println("  │ 📝 Note: Strict mode - No duplicate categories allowed" + " ".repeat(21) + "│");
        System.out.println("  └" + "─".repeat(76) + "┘");
        return null; // Valid
    }
    
    @Override
    public void updateCategory(String oldName, String newName, List<String> existingNames) {
        // Check for duplicates
        if (existingNames.contains(newName) && !newName.equals(oldName)) {
            throw new IllegalArgumentException("Duplicate category name not allowed in Strict mode: " + newName);
        }
    }
    
    @Override
    public String getStrategyName() {
        return "Strict";
    }
}
