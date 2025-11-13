package com.helpdesk.repository;

import com.helpdesk.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find by email (without deleted check)
    Optional<User> findByEmail(String email);

    // Find active users (not deleted)
    List<User> findByIsDeletedFalse();

    // Find by email and not deleted
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    // Check if email exists (for validation)
    boolean existsByEmail(String email);

    boolean existsByEmailAndIsDeletedFalse(String email);

    // Search functionality
    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND " +
            "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY u.createdAt DESC")
    List<User> searchActiveUsers(@Param("search") String search);

    // Find by role
    List<User> findByRoleAndIsDeletedFalse(User.UserRole role);

    // Find by status
    List<User> findByStatusAndIsDeletedFalse(User.UserStatus status);

    // Count active users
    long countByIsDeletedFalse();

    // NEW METHODS for Student Portal Integration
    // Find by username
    Optional<User> findByUsername(String username);

    // Find by username and not deleted
    Optional<User> findByUsernameAndIsDeletedFalse(String username);

    // Find by username or email
    Optional<User> findByUsernameOrEmail(String username, String email);

    // Find by username or email and not deleted
    Optional<User> findByUsernameOrEmailAndIsDeletedFalse(String username, String email);

    // Check if username exists
    boolean existsByUsername(String username);

    // Check if username exists and not deleted
    boolean existsByUsernameAndIsDeletedFalse(String username);

    // Find by username and enabled
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.enabled = true AND u.isDeleted = false")
    Optional<User> findByUsernameAndEnabledTrue(@Param("username") String username);

    // Find students (for reports)
    List<User> findByRoleAndIsDeletedFalseAndEnabledTrue(User.UserRole role);

    // ============================================
    // ADMIN PORTAL REPOSITORY METHODS (NEW)
    // ============================================

    // Find all users with pagination (not deleted)
    Page<User> findByIsDeletedFalse(Pageable pageable);

    // Search users by name, email, or username
    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND " +
           "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);

    // Find users by role with pagination
    Page<User> findByRoleAndIsDeletedFalse(User.UserRole role, Pageable pageable);

    // Get top 5 students by ticket count
    @Query("SELECT u.fullName, u.username, COUNT(t) as ticketCount " +
           "FROM User u LEFT JOIN u.tickets t " +
           "WHERE u.role = 'STUDENT' AND u.isDeleted = false " +
           "GROUP BY u.id, u.fullName, u.username " +
           "ORDER BY ticketCount DESC")
    List<Object[]> getTop5StudentsByTicketCount();
}