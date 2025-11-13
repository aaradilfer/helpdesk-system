package com.helpdesk.dto;

public class PaymentStatsDTO {

    private Long totalTickets = 0L;
    private Long pendingTickets = 0L;
    private Long resolvedTickets = 0L;
    private Long escalatedTickets = 0L;
    private Double totalVerifiedAmount = 0.0;

    // Constructors
    public PaymentStatsDTO() {}

    public PaymentStatsDTO(Long totalTickets, Long pendingTickets, Long resolvedTickets,
                           Long escalatedTickets, Double totalVerifiedAmount) {
        this.totalTickets = totalTickets != null ? totalTickets : 0L;
        this.pendingTickets = pendingTickets != null ? pendingTickets : 0L;
        this.resolvedTickets = resolvedTickets != null ? resolvedTickets : 0L;
        this.escalatedTickets = escalatedTickets != null ? escalatedTickets : 0L;
        this.totalVerifiedAmount = totalVerifiedAmount != null ? totalVerifiedAmount : 0.0;
    }

    // Getters and Setters
    public Long getTotalTickets() {
        return totalTickets != null ? totalTickets : 0L;
    }

    public void setTotalTickets(Long totalTickets) {
        this.totalTickets = totalTickets != null ? totalTickets : 0L;
    }

    public Long getPendingTickets() {
        return pendingTickets != null ? pendingTickets : 0L;
    }

    public void setPendingTickets(Long pendingTickets) {
        this.pendingTickets = pendingTickets != null ? pendingTickets : 0L;
    }

    public Long getResolvedTickets() {
        return resolvedTickets != null ? resolvedTickets : 0L;
    }

    public void setResolvedTickets(Long resolvedTickets) {
        this.resolvedTickets = resolvedTickets != null ? resolvedTickets : 0L;
    }

    public Long getEscalatedTickets() {
        return escalatedTickets != null ? escalatedTickets : 0L;
    }

    public void setEscalatedTickets(Long escalatedTickets) {
        this.escalatedTickets = escalatedTickets != null ? escalatedTickets : 0L;
    }

    public Double getTotalVerifiedAmount() {
        return totalVerifiedAmount != null ? totalVerifiedAmount : 0.0;
    }

    public void setTotalVerifiedAmount(Double totalVerifiedAmount) {
        this.totalVerifiedAmount = totalVerifiedAmount != null ? totalVerifiedAmount : 0.0;
    }
}

