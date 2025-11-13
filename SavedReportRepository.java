package com.helpdesk.repository;

import com.helpdesk.entity.SavedReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedReportRepository extends JpaRepository<SavedReport, Long> {

    // Find by created by
    List<SavedReport> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    // Find all ordered by creation date
    List<SavedReport> findAllByOrderByCreatedAtDesc();

    // Check if name exists for a user
    boolean existsByNameAndCreatedBy(String name, String createdBy);
}