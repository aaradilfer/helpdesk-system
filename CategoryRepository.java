package com.helpdesk.repository;

import com.helpdesk.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find active categories
    List<Category> findByIsActiveTrue();

    // Find by name
    Optional<Category> findByName(String name);

    // Check if name exists (for validation)
    boolean existsByName(String name);

    // Find categories with ticket count
    @Query("SELECT c, COUNT(t) FROM Category c LEFT JOIN c.tickets t GROUP BY c.id ORDER BY c.name")
    List<Object[]> findCategoriesWithTicketCount();
}