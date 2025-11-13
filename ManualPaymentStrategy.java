package com.helpdesk.strategy;

import com.helpdesk.entity.Ticket;
import org.springframework.stereotype.Component;

/**
 * Manual payment verification strategy
 * Requires staff to manually verify payments (accepts any valid amount)
 */
@Component("manualPaymentStrategy")
public class ManualPaymentStrategy implements PaymentStrategy {
    
    @Override
    public boolean verifyPayment(Ticket ticket) {
        System.out.println("\n  ┌" + "─".repeat(76) + "┐");
        System.out.println("  │ 📋 MANUAL PAYMENT STRATEGY - Verification Process" + " ".repeat(24) + "│");
        System.out.println("  ├" + "─".repeat(76) + "┤");
        System.out.println("  │ Strategy Type: Manual Verification (Requires Staff Approval)" + " ".repeat(15) + "│");
        System.out.println("  │ Amount: Rs. " + (ticket.getAmount() != null ? String.format("%-62s", ticket.getAmount()) : "N/A" + " ".repeat(59)) + "│");
        System.out.println("  ├" + "─".repeat(76) + "┤");
        
        // Manual strategy accepts any valid amount (staff will verify)
        if (ticket.getAmount() != null && ticket.getAmount().doubleValue() > 0) {
            System.out.println("  │ ✅ Decision: ACCEPTED FOR MANUAL REVIEW" + " ".repeat(35) + "│");
            System.out.println("  │ 📝 Note: Payment requires staff verification before final approval" + " ".repeat(9) + "│");
            System.out.println("  └" + "─".repeat(76) + "┘\n");
            return true;
        }
        
        System.out.println("  │ ❌ Decision: REJECTED (Invalid Amount)" + " ".repeat(37) + "│");
        System.out.println("  │ 📝 Reason: Amount must be greater than Rs. 0" + " ".repeat(31) + "│");
        System.out.println("  └" + "─".repeat(76) + "┘\n");
        return false;
    }
    
    @Override
    public String updateStatus(Ticket ticket, String status) {
        ticket.setStatus(Ticket.Status.valueOf(status.toUpperCase().replace(" ", "_")));
        return "Status updated to " + status + " manually";
    }
    
    @Override
    public String getStrategyName() {
        return "Manual";
    }
}

