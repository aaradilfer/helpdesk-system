package com.helpdesk.repository;

import com.helpdesk.entity.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for PaymentTransaction entity
 * Provides data access methods for payment transactions
 */
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    /**
     * Find transaction by unique transaction number
     */
    Optional<PaymentTransaction> findByTransactionNumber(String transactionNumber);

    /**
     * Find all transactions by status
     */
    List<PaymentTransaction> findByStatus(PaymentTransaction.Status status);

    /**
     * Find transactions by student ID
     */
    List<PaymentTransaction> findByStudentId(String studentId);

    /**
     * Find transactions by verification status
     */
    List<PaymentTransaction> findByVerified(Boolean verified);

    /**
     * Find transactions by category
     */
    List<PaymentTransaction> findByCategoryId(Long categoryId);

    /**
     * Count transactions by status
     */
    Long countByStatus(PaymentTransaction.Status status);

    /**
     * Count verified transactions
     */
    Long countByVerified(Boolean verified);

    /**
     * Sum of amounts for verified transactions
     */
    @Query("SELECT COALESCE(SUM(pt.amount), 0) FROM PaymentTransaction pt WHERE pt.verified = :verified")
    BigDecimal sumAmountByVerified(@Param("verified") Boolean verified);

    /**
     * Sum of amounts by status
     */
    @Query("SELECT COALESCE(SUM(pt.amount), 0) FROM PaymentTransaction pt WHERE pt.status = :status")
    BigDecimal sumAmountByStatus(@Param("status") PaymentTransaction.Status status);

    /**
     * Search transactions (student name, ID, email, transaction number)
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE " +
           "LOWER(pt.studentName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pt.studentId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pt.studentEmail) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pt.transactionNumber) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<PaymentTransaction> searchTransactions(@Param("search") String search, Pageable pageable);

    /**
     * Find all transactions with pagination and sorting
     */
    @NonNull
    Page<PaymentTransaction> findAll(@NonNull Pageable pageable);

    /**
     * Advanced search with filters
     */
    @Query("SELECT pt FROM PaymentTransaction pt WHERE " +
           "(:status IS NULL OR pt.status = :status) AND " +
           "(:categoryId IS NULL OR pt.category.id = :categoryId) AND " +
           "(:verified IS NULL OR pt.verified = :verified) AND " +
           "(:search IS NULL OR " +
           "LOWER(pt.studentName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pt.studentId) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(pt.transactionNumber) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<PaymentTransaction> findWithFilters(
            @Param("status") PaymentTransaction.Status status,
            @Param("categoryId") Long categoryId,
            @Param("verified") Boolean verified,
            @Param("search") String search,
            Pageable pageable
    );

    /**
     * Get latest transaction number for auto-generation
     */
    @Query("SELECT pt.transactionNumber FROM PaymentTransaction pt ORDER BY pt.id DESC")
    List<String> findLatestTransactionNumber(Pageable pageable);
}

