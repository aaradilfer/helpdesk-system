package com.helpdesk.config;

import com.helpdesk.strategy.PaymentStrategy;
import com.helpdesk.strategy.ManualPaymentStrategy;
import com.helpdesk.strategy.AutomatedPaymentStrategy;
import com.helpdesk.strategy.CategoryStrategy;
import com.helpdesk.strategy.StrictCategoryStrategy;
import com.helpdesk.strategy.LenientCategoryStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Payment and Category Strategies
 * Allows switching between different strategy implementations via application.properties
 */
@Configuration
public class PaymentStrategyConfig {
    
    @Value("${payment.strategy.type:manual}")
    private String paymentStrategyType;
    
    @Value("${category.strategy.type:strict}")
    private String categoryStrategyType;
    
    /**
     * Payment Strategy Bean
     * @return PaymentStrategy instance based on configuration
     */
    @Bean
    public PaymentStrategy paymentStrategy() {
        if ("automated".equalsIgnoreCase(paymentStrategyType)) {
            System.out.println("✓ Payment Strategy: AUTOMATED (Auto-verify if amount > Rs. 500)");
            return new AutomatedPaymentStrategy();
        }
        System.out.println("✓ Payment Strategy: MANUAL (Requires staff verification)");
        return new ManualPaymentStrategy(); // Default
    }
    
    /**
     * Category Strategy Bean
     * @return CategoryStrategy instance based on configuration
     */
    @Bean
    public CategoryStrategy categoryStrategy() {
        if ("lenient".equalsIgnoreCase(categoryStrategyType)) {
            System.out.println("✓ Category Strategy: LENIENT (Allows duplicate categories)");
            return new LenientCategoryStrategy();
        }
        System.out.println("✓ Category Strategy: STRICT (No duplicate categories)");
        return new StrictCategoryStrategy(); // Default
    }
}

