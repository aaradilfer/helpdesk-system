package com.helpdesk.repository;

import com.helpdesk.entity.SupportArticle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportArticleRepository extends JpaRepository<SupportArticle, Long> {

    List<SupportArticle> findByStatus(String status);

    List<SupportArticle> findByCategoryIdAndStatus(Long categoryId, String status);

    @Query("SELECT COUNT(a) FROM SupportArticle a WHERE a.status = 'Published'")
    Long countPublishedArticles();

    @Query("SELECT COUNT(a) FROM SupportArticle a WHERE a.status = 'Draft'")
    Long countDraftArticles();

    @Query("SELECT COUNT(a) FROM SupportArticle a WHERE a.status = 'Archived'")
    Long countArchivedArticles();

    @Query("SELECT COALESCE(SUM(a.viewCount), 0) FROM SupportArticle a")
    Long getTotalViews();

    @Query("SELECT a FROM SupportArticle a WHERE " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.content) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(a.keywords) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND a.status = 'Published'")
    List<SupportArticle> searchPublishedArticles(@Param("query") String query);

    List<SupportArticle> findByCategoryId(Long categoryId);
}

