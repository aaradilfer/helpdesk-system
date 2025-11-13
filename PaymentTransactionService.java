package com.helpdesk.service;

import com.helpdesk.entity.PaymentTransaction;
import com.helpdesk.entity.Category;
import com.helpdesk.repository.PaymentTransactionRepository;
import com.helpdesk.repository.CategoryRepository;
import com.helpdesk.strategy.PaymentStrategy;
import com.helpdesk.strategy.CategoryStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Service for Payment Transaction operations
 * Provides full CRUD functionality for payment transactions
 * Implements Strategy Pattern for flexible payment verification and category validation
 */
@Service
@Transactional
public class PaymentTransactionService {

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PaymentStrategy paymentStrategy; // Strategy Pattern for flexible payment verification

    @Autowired
    private CategoryStrategy categoryStrategy; // Strategy Pattern for category validation

    /**
     * CREATE: Create new payment transaction
     * Uses Strategy Pattern for category validation and auto-verification
     */
    public PaymentTransaction createTransaction(PaymentTransaction transaction, String createdBy) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🎯 STRATEGY PATTERN DEMONSTRATION - CREATING PAYMENT TRANSACTION");
        System.out.println("=".repeat(80));
        
        // Auto-generate transaction number
        String transactionNumber = generateTransactionNumber();
        transaction.setTransactionNumber(transactionNumber);
        System.out.println("📋 Transaction Number: " + transactionNumber);
        System.out.println("👤 Student: " + transaction.getStudentName() + " (" + transaction.getStudentId() + ")");
        System.out.println("💰 Amount: Rs. " + transaction.getAmount());
        System.out.println("📁 Category: " + (transaction.getCategory() != null ? transaction.getCategory().getName() : "N/A"));
        
        // STRATEGY PATTERN #1: Category Validation
        System.out.println("\n" + "-".repeat(80));
        System.out.println("🔍 STRATEGY #1: CATEGORY VALIDATION");
        System.out.println("-".repeat(80));
        if (transaction.getCategory() != null) {
            System.out.println("✓ Active Strategy: " + categoryStrategy.getStrategyName());
            boolean isValid = categoryStrategy.validateCategory(transaction.getCategory());
            System.out.println("✓ Validation Result: " + (isValid ? "PASSED ✅" : "FAILED ❌"));
            
            if (!isValid) {
                System.out.println("⚠️  Category validation failed according to " + categoryStrategy.getStrategyName());
            }
        } else {
            System.out.println("⚠️  No category provided - skipping validation");
        }
        
        // Set timestamps
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());
        
        // Set initial status
        if (transaction.getStatus() == null) {
            transaction.setStatus(PaymentTransaction.Status.PENDING);
        }
        
        // Set verified flag
        if (transaction.getVerified() == null) {
            transaction.setVerified(false);
        }
        
        // Set creator
        transaction.setLastModifiedBy(createdBy);
        
        // STRATEGY PATTERN #2: Automated Payment Verification (if enabled)
        System.out.println("\n" + "-".repeat(80));
        System.out.println("🔍 STRATEGY #2: PAYMENT VERIFICATION");
        System.out.println("-".repeat(80));
        System.out.println("✓ Active Strategy: " + paymentStrategy.getStrategyName());
        
        // Note: We'll use the strategy for verification, not creation
        // Verification happens in verifyTransaction() method
        System.out.println("ℹ️  Payment verification will be applied when verify() is called");
        System.out.println("ℹ️  Current Status: " + transaction.getStatus());
        
        PaymentTransaction saved = paymentTransactionRepository.save(transaction);
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ TRANSACTION CREATED SUCCESSFULLY");
        System.out.println("📝 Transaction ID: " + saved.getId());
        System.out.println("📋 Transaction Number: " + saved.getTransactionNumber());
        System.out.println("📊 Status: " + saved.getStatus());
        System.out.println("=".repeat(80) + "\n");
        
        return saved;
    }

    /**
     * READ: Get transaction by ID
     */
    public Optional<PaymentTransaction> getTransactionById(Long id) {
        return paymentTransactionRepository.findById(id);
    }

    /**
     * READ: Get all transactions with pagination
     */
    public Page<PaymentTransaction> getAllTransactions(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                    ? Sort.by(sortBy).ascending() 
                    : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return paymentTransactionRepository.findAll(pageable);
    }

    /**
     * READ: Get transactions by status
     */
    public List<PaymentTransaction> getTransactionsByStatus(PaymentTransaction.Status status) {
        return paymentTransactionRepository.findByStatus(status);
    }

    /**
     * READ: Get transactions by student ID
     */
    public List<PaymentTransaction> getTransactionsByStudentId(String studentId) {
        return paymentTransactionRepository.findByStudentId(studentId);
    }

    /**
     * READ: Search transactions
     */
    public Page<PaymentTransaction> searchTransactions(String search, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                    ? Sort.by(sortBy).ascending() 
                    : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        if (search != null && !search.trim().isEmpty()) {
            return paymentTransactionRepository.searchTransactions(search, pageable);
        }
        return paymentTransactionRepository.findAll(pageable);
    }

    /**
     * READ: Advanced search with filters
     */
    public Page<PaymentTransaction> searchWithFilters(
            String statusStr,
            Long categoryId,
            Boolean verified,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") 
                    ? Sort.by(sortBy).ascending() 
                    : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        PaymentTransaction.Status status = null;
        if (statusStr != null && !statusStr.isEmpty()) {
            try {
                status = PaymentTransaction.Status.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }
        
        return paymentTransactionRepository.findWithFilters(status, categoryId, verified, search, pageable);
    }

    /**
     * UPDATE: Update existing transaction
     */
    public PaymentTransaction updateTransaction(Long id, PaymentTransaction updatedTransaction, String modifiedBy) {
        Optional<PaymentTransaction> existingOpt = paymentTransactionRepository.findById(id);
        
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("Transaction not found with id: " + id);
        }
        
        PaymentTransaction existing = existingOpt.get();
        
        // Update fields
        existing.setStudentName(updatedTransaction.getStudentName());
        existing.setStudentId(updatedTransaction.getStudentId());
        existing.setStudentEmail(updatedTransaction.getStudentEmail());
        existing.setStudentPhone(updatedTransaction.getStudentPhone());
        existing.setCategory(updatedTransaction.getCategory());
        existing.setAmount(updatedTransaction.getAmount());
        existing.setPaymentMethod(updatedTransaction.getPaymentMethod());
        existing.setReferenceNumber(updatedTransaction.getReferenceNumber());
        existing.setDescription(updatedTransaction.getDescription());
        existing.setStatus(updatedTransaction.getStatus());
        
        // Update attachment if provided
        if (updatedTransaction.getAttachmentFilename() != null) {
            existing.setAttachmentFilename(updatedTransaction.getAttachmentFilename());
        }
        
        // Update metadata
        existing.setUpdatedAt(LocalDateTime.now());
        existing.setLastModifiedBy(modifiedBy);
        
        return paymentTransactionRepository.save(existing);
    }

    /**
     * UPDATE: Verify transaction
     * Uses Strategy Pattern for flexible payment verification
     */
    public PaymentTransaction verifyTransaction(Long id, Boolean verified, String verifiedBy) {
        Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository.findById(id);
        
        if (transactionOpt.isEmpty()) {
            throw new RuntimeException("Transaction not found with id: " + id);
        }
        
        PaymentTransaction transaction = transactionOpt.get();
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🎯 STRATEGY PATTERN DEMONSTRATION - VERIFYING PAYMENT");
        System.out.println("=".repeat(80));
        System.out.println("📋 Transaction: " + transaction.getTransactionNumber());
        System.out.println("💰 Amount: Rs. " + transaction.getAmount());
        System.out.println("👤 Student: " + transaction.getStudentName());
        System.out.println("📁 Category: " + (transaction.getCategory() != null ? transaction.getCategory().getName() : "N/A"));
        
        System.out.println("\n" + "-".repeat(80));
        System.out.println("🔍 STRATEGY PATTERN: PAYMENT VERIFICATION");
        System.out.println("-".repeat(80));
        System.out.println("✓ Active Strategy: " + paymentStrategy.getStrategyName());
        System.out.println("✓ Manual Verification Request: " + (verified ? "APPROVE ✅" : "REJECT ❌"));
        
        // STRATEGY PATTERN: Use configured payment strategy
        // Create a temporary ticket-like object for strategy compatibility
        com.helpdesk.entity.Ticket tempTicket = new com.helpdesk.entity.Ticket();
        // Convert BigDecimal to Double for Ticket entity compatibility
        if (transaction.getAmount() != null) {
            tempTicket.setAmount(transaction.getAmount().doubleValue());
        }
        
        boolean strategyResult = paymentStrategy.verifyPayment(tempTicket);
        System.out.println("✓ Strategy Recommendation: " + (strategyResult ? "AUTO-APPROVE ✅" : "MANUAL REVIEW REQUIRED ⚠️"));
        
        // Final decision: Manual verification overrides strategy if provided
        boolean finalVerification = verified;
        System.out.println("✓ Final Decision: " + (finalVerification ? "VERIFIED ✅" : "REJECTED ❌"));
        
        transaction.setVerified(finalVerification);
        transaction.setVerifiedBy(verifiedBy);
        transaction.setVerifiedAt(LocalDateTime.now());
        
        // Update status based on verification
        if (finalVerification) {
            transaction.setStatus(PaymentTransaction.Status.VERIFIED);
            System.out.println("📊 Status Updated: PENDING → VERIFIED");
        } else {
            transaction.setStatus(PaymentTransaction.Status.REJECTED);
            System.out.println("📊 Status Updated: PENDING → REJECTED");
        }
        
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setLastModifiedBy(verifiedBy);
        
        PaymentTransaction saved = paymentTransactionRepository.save(transaction);
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ VERIFICATION COMPLETED");
        System.out.println("📝 Verified By: " + verifiedBy);
        System.out.println("🕒 Verified At: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("=".repeat(80) + "\n");
        
        return saved;
    }

    /**
     * UPDATE: Update transaction status
     */
    public PaymentTransaction updateStatus(Long id, PaymentTransaction.Status status, String modifiedBy) {
        Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository.findById(id);
        
        if (transactionOpt.isEmpty()) {
            throw new RuntimeException("Transaction not found with id: " + id);
        }
        
        PaymentTransaction transaction = transactionOpt.get();
        transaction.setStatus(status);
        transaction.setUpdatedAt(LocalDateTime.now());
        transaction.setLastModifiedBy(modifiedBy);
        
        return paymentTransactionRepository.save(transaction);
    }

    /**
     * DELETE: Delete transaction
     */
    public void deleteTransaction(Long id) {
        paymentTransactionRepository.deleteById(id);
    }

    /**
     * STATISTICS: Get pending transactions count
     */
    public Long getPendingCount() {
        return paymentTransactionRepository.countByStatus(PaymentTransaction.Status.PENDING);
    }

    /**
     * STATISTICS: Get verified transactions count
     */
    public Long getVerifiedCount() {
        return paymentTransactionRepository.countByVerified(true);
    }

    /**
     * STATISTICS: Get rejected transactions count
     */
    public Long getRejectedCount() {
        return paymentTransactionRepository.countByStatus(PaymentTransaction.Status.REJECTED);
    }

    /**
     * STATISTICS: Get escalated transactions count
     */
    public Long getEscalatedCount() {
        return paymentTransactionRepository.countByStatus(PaymentTransaction.Status.ESCALATED);
    }

    /**
     * STATISTICS: Get total verified amount
     */
    public BigDecimal getTotalVerifiedAmount() {
        BigDecimal total = paymentTransactionRepository.sumAmountByVerified(true);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * STATISTICS: Get total pending amount
     */
    public BigDecimal getTotalPendingAmount() {
        BigDecimal total = paymentTransactionRepository.sumAmountByStatus(PaymentTransaction.Status.PENDING);
        return total != null ? total : BigDecimal.ZERO;
    }

    /**
     * HELPER: Generate unique transaction number
     * Format: TXN-YYYY-NNNN (e.g., TXN-2025-0001)
     */
    private String generateTransactionNumber() {
        String year = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy"));
        String prefix = "TXN-" + year + "-";
        
        // Get latest transaction number
        Pageable pageable = PageRequest.of(0, 1);
        List<String> latestNumbers = paymentTransactionRepository.findLatestTransactionNumber(pageable);
        
        int nextNumber = 1;
        
        if (!latestNumbers.isEmpty()) {
            String latestNumber = latestNumbers.get(0);
            if (latestNumber != null && latestNumber.startsWith(prefix)) {
                try {
                    String numberPart = latestNumber.substring(prefix.length());
                    nextNumber = Integer.parseInt(numberPart) + 1;
                } catch (Exception e) {
                    // If parsing fails, start from 1
                    nextNumber = 1;
                }
            }
        }
        
        return prefix + String.format("%04d", nextNumber);
    }

    /**
     * HELPER: Get all categories for dropdown
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * HELPER: Get active categories only
     */
    public List<Category> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }
}

