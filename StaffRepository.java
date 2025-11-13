package com.helpdesk.repository;

import com.helpdesk.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    // Find active staff
    List<Staff> findByIsActiveTrue();

    // Find by email
    Optional<Staff> findByEmail(String email);

    // Check if email exists (for validation)
    boolean existsByEmail(String email);

    // Find by department
    List<Staff> findByDepartmentAndIsActiveTrue(String department);

    // Find staff with assigned ticket count
    @Query("SELECT s, COUNT(t) FROM Staff s LEFT JOIN s.assignedTickets t GROUP BY s.id ORDER BY s.name")
    List<Object[]> findStaffWithTicketCount();

    // Get all departments
    @Query("SELECT DISTINCT s.department FROM Staff s WHERE s.department IS NOT NULL AND s.isActive = true ORDER BY s.department")
    List<String> findAllDepartments();
}