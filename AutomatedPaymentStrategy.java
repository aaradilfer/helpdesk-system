package com.helpdesk.strategy;

import com.helpdesk.entity.Ticket;
import org.springframework.stereotype.Component;

/**
 * Automated payment verification strategy
 * Automatically verifies payments if amount exceeds minimum threshold (Rs. 500)
 */
@Component("automatedPaymentStrategy")
public class AutomatedPaymentStrategy implements PaymentStrategy {
    
    private static final double MINIMUM_AMOUNT = 500.0;
    
    @Override
    public boolean verifyPayment(Ticket ticket) {
        System.out.println("\n  ┌" + "─".repeat(76) + "┐");
        System.out.println("  │ 🤖 AUTOMATED PAYMENT STRATEGY - Verification Process" + " ".repeat(20) + "│");
        System.out.println("  ├" + "─".repeat(76) + "┤");
        System.out.println("  │ Strategy Type: Automated Verification (No Staff Required)" + " ".repeat(17) + "│");
        System.out.println("  │ Amount: Rs. " + (ticket.getAmount() != null ? String.format("%-62s", ticket.getAmount()) : "N/A" + " ".repeat(59)) + "│");
        System.out.println("  │ Minimum Threshold: Rs. " + String.format("%-50s", MINIMUM_AMOUNT) + "│");
        System.out.println("  ├" + "─".repeat(76) + "┤");
        
        // Auto-verify if amount exceeds minimum threshold
        if (ticket.getAmount() != null && ticket.getAmount().doubleValue() > MINIMUM_AMOUNT) {
            System.out.println("  │ ✅ Decision: AUTO-APPROVED (Amount > Rs. " + MINIMUM_AMOUNT + ")" + " ".repeat(30) + "│");
            System.out.println("  │ 📝 Note: Payment automatically verified by system" + " ".repeat(26) + "│");
            System.out.println("  │ 🚀 Action: No manual verification required" + " ".repeat(33) + "│");
            System.out.println("  └" + "─".repeat(76) + "┘\n");
            return true;
        }
        
        System.out.println("  │ ⚠️  Decision: REQUIRES MANUAL REVIEW" + " ".repeat(39) + "│");
        System.out.println("  │ 📝 Reason: Amount below minimum threshold (Rs. " + MINIMUM_AMOUNT + ")" + " ".repeat(21) + "│");
        System.out.println("  │ 👤 Action: Escalate to staff for manual verification" + " ".repeat(23) + "│");
        System.out.println("  └" + "─".repeat(76) + "┘\n");
        return false;
    }
    
    @Override
    public String updateStatus(Ticket ticket, String status) {
        ticket.setStatus(Ticket.Status.valueOf(status.toUpperCase().replace(" ", "_")));
        return "Status updated to " + status + " automatically";
    }
    
    @Override
    public String getStrategyName() {
        return "Automated";
    }
}
