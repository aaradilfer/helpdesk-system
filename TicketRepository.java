package com.helpdesk.repository;

import com.helpdesk.entity.Ticket;
import com.helpdesk.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Search functionality
    @Query("SELECT t FROM Ticket t WHERE " +
            "(:searchTerm IS NULL OR " +
            "LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.studentName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(t.studentId) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Ticket> findBySearchTerm(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Filter by status
    Page<Ticket> findByStatus(Ticket.Status status, Pageable pageable);

    // Filter by priority
    Page<Ticket> findByPriority(Ticket.Priority priority, Pageable pageable);

    // Filter by category
    Page<Ticket> findByCategoryId(Long categoryId, Pageable pageable);

    // Filter by assigned staff
    Page<Ticket> findByAssignedStaffId(Long staffId, Pageable pageable);

    // Find by student
    Page<Ticket> findByStudentId(String studentId, Pageable pageable);
    Page<Ticket> findByStudentNameContainingIgnoreCase(String studentName, Pageable pageable);

    // Dashboard statistics queries
    @Query("SELECT COUNT(t) FROM Ticket t")
    Long countTotalTickets();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = 'OPEN'")
    Long countOpenTickets();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = 'IN_PROGRESS'")
    Long countInProgressTickets();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = 'RESOLVED'")
    Long countResolvedTickets();

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.status = 'CLOSED'")
    Long countClosedTickets();

    // Average resolution time
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, t.createdAt, t.resolvedAt)) FROM Ticket t WHERE t.resolvedAt IS NOT NULL")
    Double getAverageResolutionTimeInHours();

    // Tickets per category for pie chart
    @Query("SELECT c.name, COUNT(t) FROM Ticket t JOIN t.category c GROUP BY c.id, c.name")
    List<Object[]> getTicketsPerCategory();

    // Monthly tickets trend (last 12 months)
    @Query("SELECT YEAR(t.createdAt), MONTH(t.createdAt), COUNT(t) " +
            "FROM Ticket t " +
            "WHERE t.createdAt >= :startDate " +
            "GROUP BY YEAR(t.createdAt), MONTH(t.createdAt) " +
            "ORDER BY YEAR(t.createdAt), MONTH(t.createdAt)")
    List<Object[]> getMonthlyTicketTrend(@Param("startDate") LocalDateTime startDate);

    // Top 5 students by number of tickets
    @Query("SELECT t.studentName, t.studentId, COUNT(t) " +
            "FROM Ticket t " +
            "GROUP BY t.studentName, t.studentId " +
            "ORDER BY COUNT(t) DESC")
    List<Object[]> getTopStudentsByTicketCount(Pageable pageable);

    // Complex reporting query with filters
    @Query("SELECT t FROM Ticket t WHERE " +
            "(:startDate IS NULL OR DATE(t.createdAt) >= :startDate) AND " +
            "(:endDate IS NULL OR DATE(t.createdAt) <= :endDate) AND " +
            "(:categoryIds IS NULL OR t.category.id IN :categoryIds) AND " +
            "(:staffIds IS NULL OR t.assignedStaff.id IN :staffIds) AND " +
            "(:statuses IS NULL OR t.status IN :statuses) AND " +
            "(:studentName IS NULL OR LOWER(t.studentName) LIKE LOWER(CONCAT('%', :studentName, '%'))) AND " +
            "(:studentId IS NULL OR LOWER(t.studentId) LIKE LOWER(CONCAT('%', :studentId, '%')))")
    List<Ticket> findTicketsWithFilters(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("staffIds") List<Long> staffIds,
            @Param("statuses") List<Ticket.Status> statuses,
            @Param("studentName") String studentName,
            @Param("studentId") String studentId
    );

    // Count tickets with filters for reporting
    @Query("SELECT COUNT(t) FROM Ticket t WHERE " +
            "(:startDate IS NULL OR DATE(t.createdAt) >= :startDate) AND " +
            "(:endDate IS NULL OR DATE(t.createdAt) <= :endDate) AND " +
            "(:categoryIds IS NULL OR t.category.id IN :categoryIds) AND " +
            "(:staffIds IS NULL OR t.assignedStaff.id IN :staffIds) AND " +
            "(:statuses IS NULL OR t.status IN :statuses) AND " +
            "(:studentName IS NULL OR LOWER(t.studentName) LIKE LOWER(CONCAT('%', :studentName, '%'))) AND " +
            "(:studentId IS NULL OR LOWER(t.studentId) LIKE LOWER(CONCAT('%', :studentId, '%')))")
    Long countTicketsWithFilters(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("staffIds") List<Long> staffIds,
            @Param("statuses") List<Ticket.Status> statuses,
            @Param("studentName") String studentName,
            @Param("studentId") String studentId
    );

    // Average resolution time with filters
    @Query("SELECT AVG(TIMESTAMPDIFF(HOUR, t.createdAt, t.resolvedAt)) FROM Ticket t WHERE " +
            "t.resolvedAt IS NOT NULL AND " +
            "(:startDate IS NULL OR DATE(t.createdAt) >= :startDate) AND " +
            "(:endDate IS NULL OR DATE(t.createdAt) <= :endDate) AND " +
            "(:categoryIds IS NULL OR t.category.id IN :categoryIds) AND " +
            "(:staffIds IS NULL OR t.assignedStaff.id IN :staffIds) AND " +
            "(:statuses IS NULL OR t.status IN :statuses) AND " +
            "(:studentName IS NULL OR LOWER(t.studentName) LIKE LOWER(CONCAT('%', :studentName, '%'))) AND " +
            "(:studentId IS NULL OR LOWER(t.studentId) LIKE LOWER(CONCAT('%', :studentId, '%')))")
    Double getAverageResolutionTimeWithFilters(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("staffIds") List<Long> staffIds,
            @Param("statuses") List<Ticket.Status> statuses,
            @Param("studentName") String studentName,
            @Param("studentId") String studentId
    );

    // NEW METHODS for Student Portal Integration
    // Find tickets by user
    Page<Ticket> findByUser(User user, Pageable pageable);

    // Find tickets by user and status
    Page<Ticket> findByUserAndStatus(User user, Ticket.Status status, Pageable pageable);

    // Find tickets by user and priority
    Page<Ticket> findByUserAndPriority(User user, Ticket.Priority priority, Pageable pageable);

    // Find tickets by user and category
    Page<Ticket> findByUserAndCategoryId(User user, Long categoryId, Pageable pageable);

    // Complex query for student tickets with filters
    @Query("SELECT t FROM Ticket t WHERE t.user = :user AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:priority IS NULL OR t.priority = :priority) AND " +
            "(:categoryId IS NULL OR t.category.id = :categoryId) AND " +
            "(:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Ticket> findUserTicketsWithFilters(
            @Param("user") User user,
            @Param("status") Ticket.Status status,
            @Param("priority") Ticket.Priority priority,
            @Param("categoryId") Long categoryId,
            @Param("search") String search,
            Pageable pageable
    );

    // Count tickets by user
    long countByUser(User user);

    // Count tickets by user and status
    long countByUserAndStatus(User user, Ticket.Status status);

    // ============================================
    // ADMIN PORTAL REPOSITORY METHODS (NEW)
    // ============================================

    // Count by status
    long countByStatus(Ticket.Status status);

    // Count tickets created between dates
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // Count tickets by category
    @Query("SELECT c.name, COUNT(t) FROM Ticket t JOIN t.category c GROUP BY c.id, c.name")
    List<Object[]> countTicketsByCategory();

    // Count tickets by priority
    @Query("SELECT t.priority, COUNT(t) FROM Ticket t GROUP BY t.priority")
    List<Object[]> countTicketsByPriority();

    // ============================================
    // STAFF PORTAL REPOSITORY METHODS (NEW)
    // ============================================

    // Find tickets assigned to a staff member (User)
    Page<Ticket> findByAssignedTo(User assignedTo, Pageable pageable);

    // Count tickets assigned to a staff member
    long countByAssignedTo(User assignedTo);
}