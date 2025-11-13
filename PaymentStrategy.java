package com.helpdesk.strategy;

import com.helpdesk.entity.Ticket;

/**
 * Strategy interface for payment verification
 * Supports different payment verification approaches (Manual, Automated)
 */
public interface PaymentStrategy {
    /**
     * Verify payment for a ticket
     * @param ticket The ticket with payment information
     * @return true if payment is verified, false otherwise
     */
    boolean verifyPayment(Ticket ticket);
    
    /**
     * Update ticket status based on strategy
     * @param ticket The ticket to update
     * @param status The new status
     * @return Status update message
     */
    String updateStatus(Ticket ticket, String status);
    
    /**
     * Get the name of the strategy
     * @return Strategy name
     */
    String getStrategyName();
}
