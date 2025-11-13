package com.helpdesk.controller;

import com.helpdesk.dto.PaymentStatsDTO;
import com.helpdesk.service.PaymentCategoryService;
import com.helpdesk.service.PaymentTransactionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;

@Controller
@RequestMapping("/payment")
public class PaymentDashboardController {

    @Autowired
    private PaymentTransactionService paymentTransactionService;
    
    @Autowired
    private PaymentCategoryService paymentCategoryService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // Check if user is logged in (session-based authentication)
        String paymentUser = (String) session.getAttribute("paymentUser");
        if (paymentUser == null) {
            return "redirect:/payment/login";
        }

        PaymentStatsDTO stats = getPaymentStats();
        model.addAttribute("stats", stats);
        model.addAttribute("username", paymentUser);
        
        // Add current active strategies to display on dashboard
        String categoryStrategy = paymentCategoryService.getCurrentCategoryStrategyName();
        model.addAttribute("currentPaymentStrategy", "MANUAL"); // Default for PaymentTransaction
        model.addAttribute("currentCategoryStrategy", categoryStrategy);
        
        return "payment/dashboard";
    }

    private PaymentStatsDTO getPaymentStats() {
        PaymentStatsDTO stats = new PaymentStatsDTO();

        try {
            // Get transaction counts from PaymentTransactionService
            stats.setPendingTickets(paymentTransactionService.getPendingCount());
            stats.setResolvedTickets(paymentTransactionService.getVerifiedCount()); // Verified = Resolved
            stats.setEscalatedTickets(paymentTransactionService.getEscalatedCount());

            Long total = (stats.getPendingTickets() != null ? stats.getPendingTickets() : 0) +
                    (stats.getResolvedTickets() != null ? stats.getResolvedTickets() : 0) +
                    (stats.getEscalatedTickets() != null ? stats.getEscalatedTickets() : 0);
            stats.setTotalTickets(total);

            // Get total verified amount from PaymentTransactionService
            BigDecimal verifiedAmount = paymentTransactionService.getTotalVerifiedAmount();
            stats.setTotalVerifiedAmount(verifiedAmount != null ? verifiedAmount.doubleValue() : 0.0);

        } catch (Exception e) {
            // Set default values if there's an error
            stats.setPendingTickets(0L);
            stats.setResolvedTickets(0L);
            stats.setEscalatedTickets(0L);
            stats.setTotalTickets(0L);
            stats.setTotalVerifiedAmount(0.0);
        }

        return stats;
    }

    @GetMapping("")
    public String redirectToDashboard() {
        return "redirect:/payment/dashboard";
    }
}

