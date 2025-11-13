package com.helpdesk.service;

import com.helpdesk.entity.Ticket;
import com.helpdesk.repository.TicketRepository;
import com.helpdesk.strategy.PaymentStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Payment Portal Service - Adapter for existing TicketRepository
 * Provides payment-specific business logic with Strategy Pattern support
 */
@Service
@Transactional
public class PaymentTicketService {

    @Autowired
    private TicketRepository ticketRepository;
    
    @Autowired
    private PaymentStrategy paymentStrategy; // Strategy Pattern for flexible payment verification

    public Ticket createTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public List<Ticket> getTicketsByStatus(String status) {
        try {
            Ticket.Status statusEnum = Ticket.Status.valueOf(status.toUpperCase().replace(" ", "_"));
            return ticketRepository.findAll().stream()
                    .filter(t -> t.getStatus() == statusEnum)
                    .toList();
        } catch (IllegalArgumentException e) {
            return List.of();
        }
    }

    public Ticket updateTicket(Ticket ticket) {
        ticket.setUpdatedAt(LocalDateTime.now());
        return ticketRepository.save(ticket);
    }

    /**
     * Verify payment ticket using active PaymentStrategy
     * Supports both Manual and Automated verification strategies
     */
    public Ticket verifyTicket(Long ticketId, Boolean verified, String modifiedBy) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            
            // STRATEGY PATTERN: Use configured strategy for verification
            boolean isVerified = paymentStrategy.verifyPayment(ticket);
            ticket.setVerified(isVerified);
            
            // Keep existing functionality
            ticket.setLastModifiedBy(modifiedBy);
            ticket.setUpdatedAt(LocalDateTime.now());
            if (isVerified) {
                ticket.setStatus(Ticket.Status.CLOSED);
            }
            
            System.out.println("[PaymentTicketService] Verification completed using " + 
                             paymentStrategy.getStrategyName() + " strategy");
            
            return ticketRepository.save(ticket);
        }
        throw new RuntimeException("Ticket not found with id: " + ticketId);
    }

    public Ticket updateTicketStatus(Long ticketId, String status, String modifiedBy) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            try {
                Ticket.Status statusEnum = Ticket.Status.valueOf(status.toUpperCase().replace(" ", "_"));
                ticket.setStatus(statusEnum);
            } catch (IllegalArgumentException e) {
                // If status string doesn't match enum, keep current status
            }
            ticket.setLastModifiedBy(modifiedBy);
            ticket.setUpdatedAt(LocalDateTime.now());
            return ticketRepository.save(ticket);
        }
        throw new RuntimeException("Ticket not found with id: " + ticketId);
    }

    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }

    // Statistics methods for Payment Portal Dashboard
    public Long getPendingTicketsCount() {
        try {
            // Count tickets with OPEN status (treated as Pending in Payment Portal)
            return ticketRepository.countByStatus(Ticket.Status.OPEN);
        } catch (Exception e) {
            return 0L;
        }
    }

    public Long getResolvedTicketsCount() {
        try {
            return ticketRepository.countByStatus(Ticket.Status.RESOLVED);
        } catch (Exception e) {
            return 0L;
        }
    }

    public Long getEscalatedTicketsCount() {
        try {
            // Count tickets with IN_PROGRESS status (treated as Escalated in Payment Portal)
            return ticketRepository.countByStatus(Ticket.Status.IN_PROGRESS);
        } catch (Exception e) {
            return 0L;
        }
    }

    public Double getTotalVerifiedAmount() {
        try {
            // Calculate total amount for verified tickets
            List<Ticket> allTickets = ticketRepository.findAll();
            return allTickets.stream()
                    .filter(t -> t.getVerified() != null && t.getVerified())
                    .filter(t -> t.getAmount() != null)
                    .mapToDouble(Ticket::getAmount)
                    .sum();
        } catch (Exception e) {
            return 0.0;
        }
    }

    public Long getTotalTicketsCount() {
        return ticketRepository.count();
    }

    public Long getVerifiedTicketsCount() {
        try {
            List<Ticket> allTickets = ticketRepository.findAll();
            return allTickets.stream()
                    .filter(t -> t.getVerified() != null && t.getVerified())
                    .count();
        } catch (Exception e) {
            return 0L;
        }
    }
    
    /**
     * Get the name of the currently active payment strategy
     * @return Strategy name (e.g., "Manual" or "Automated")
     */
    public String getCurrentPaymentStrategyName() {
        return paymentStrategy.getStrategyName();
    }
}

