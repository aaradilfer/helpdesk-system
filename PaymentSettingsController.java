package com.helpdesk.controller;

import com.helpdesk.service.PaymentCategoryService;
import com.helpdesk.service.PaymentTicketService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for Payment Portal Strategy Settings
 * Allows admin to switch between different payment and category strategies
 */
@Controller
@RequestMapping("/payment/settings")
public class PaymentSettingsController {

    @Autowired
    private PaymentTicketService paymentTicketService;

    @Autowired
    private PaymentCategoryService paymentCategoryService;

    @Autowired
    private ConfigurableEnvironment environment;

    @Value("${payment.strategy.type:manual}")
    private String currentPaymentStrategy;

    @Value("${category.strategy.type:strict}")
    private String currentCategoryStrategy;

    /**
     * Show strategy settings page
     */
    @GetMapping
    public String showSettingsPage(HttpSession session, Model model) {
        // Check if payment user is logged in
        String paymentUser = (String) session.getAttribute("paymentUser");
        if (paymentUser == null) {
            return "redirect:/payment/login?error=session_expired";
        }

        // Get current strategies
        String activePaymentStrategy = paymentTicketService.getCurrentPaymentStrategyName();
        String activeCategoryStrategy = paymentCategoryService.getCurrentCategoryStrategyName();

        model.addAttribute("currentPaymentStrategy", activePaymentStrategy);
        model.addAttribute("currentCategoryStrategy", activeCategoryStrategy);
        model.addAttribute("paymentUser", paymentUser);

        return "payment/settings";
    }

    /**
     * Update strategy settings
     * NOTE: This updates runtime configuration only
     * For permanent changes, update application.properties
     */
    @PostMapping("/update")
    public String updateSettings(
            HttpSession session,
            @RequestParam String paymentStrategy,
            @RequestParam String categoryStrategy,
            RedirectAttributes redirectAttributes) {

        // Check if payment user is logged in
        String paymentUser = (String) session.getAttribute("paymentUser");
        if (paymentUser == null) {
            return "redirect:/payment/login?error=session_expired";
        }

        try {
            // Create a runtime property source for strategy changes
            Map<String, Object> props = new HashMap<>();
            props.put("payment.strategy.type", paymentStrategy);
            props.put("category.strategy.type", categoryStrategy);
            
            // Add as high priority property source
            environment.getPropertySources().addFirst(
                new MapPropertySource("strategyOverride", props)
            );

            redirectAttributes.addFlashAttribute("successMessage", 
                "Strategy settings updated successfully! " +
                "Payment: " + paymentStrategy.toUpperCase() + ", " +
                "Category: " + categoryStrategy.toUpperCase() + ". " +
                "Note: Changes are temporary. Update application.properties for permanent changes.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating strategy settings: " + e.getMessage());
        }

        return "redirect:/payment/settings";
    }
}

