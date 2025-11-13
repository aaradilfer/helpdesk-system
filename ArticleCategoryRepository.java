package com.helpdesk.repository;

import com.helpdesk.entity.ArticleCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleCategoryRepository extends JpaRepository<ArticleCategory, Long> {

    List<ArticleCategory> findByIsActiveTrue();

    @Query("SELECT COUNT(c) FROM ArticleCategory c WHERE c.name = :name")
    long countByName(@Param("name") String name);

    Optional<ArticleCategory> findByName(String name);
}

