package com.helpdesk.service;

import com.helpdesk.entity.SupportArticle;
import com.helpdesk.repository.SupportArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SupportArticleService {

    @Autowired
    private SupportArticleRepository supportArticleRepository;

    public SupportArticle createArticle(SupportArticle article) {
        return supportArticleRepository.save(article);
    }

    public Optional<SupportArticle> getArticleById(Long id) {
        Optional<SupportArticle> article = supportArticleRepository.findById(id);
        if (article.isPresent()) {
            // Increment view count when article is retrieved (for public views)
            SupportArticle existingArticle = article.get();
            existingArticle.setViewCount(existingArticle.getViewCount() + 1);
            supportArticleRepository.save(existingArticle);
        }
        return article;
    }

    public List<SupportArticle> getAllArticles() {
        return supportArticleRepository.findAll();
    }

    public List<SupportArticle> getArticlesByStatus(String status) {
        return supportArticleRepository.findByStatus(status);
    }

    public List<SupportArticle> getPublishedArticles() {
        return supportArticleRepository.findByStatus("Published");
    }

    public List<SupportArticle> searchArticles(String query) {
        return supportArticleRepository.searchPublishedArticles(query);
    }

    public SupportArticle updateArticle(SupportArticle article) {
        article.setUpdatedAt(LocalDateTime.now());
        return supportArticleRepository.save(article);
    }

    public void deleteArticle(Long id) {
        supportArticleRepository.deleteById(id);
    }

    public SupportArticle updateArticleStatus(Long articleId, String status, String modifiedBy) {
        Optional<SupportArticle> articleOpt = supportArticleRepository.findById(articleId);
        if (articleOpt.isPresent()) {
            SupportArticle article = articleOpt.get();
            article.setStatus(status);
            article.setLastModifiedBy(modifiedBy);
            return supportArticleRepository.save(article);
        }
        throw new RuntimeException("Article not found with id: " + articleId);
    }

    public void recordFeedback(Long articleId, boolean isHelpful) {
        Optional<SupportArticle> articleOpt = supportArticleRepository.findById(articleId);
        if (articleOpt.isPresent()) {
            SupportArticle article = articleOpt.get();
            if (isHelpful) {
                article.setHelpfulCount(article.getHelpfulCount() + 1);
            } else {
                article.setNotHelpfulCount(article.getNotHelpfulCount() + 1);
            }
            supportArticleRepository.save(article);
        }
    }

    // Statistics methods
    public Long getPublishedArticlesCount() {
        try {
            Long count = supportArticleRepository.countPublishedArticles();
            return count != null ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    public Long getDraftArticlesCount() {
        try {
            Long count = supportArticleRepository.countDraftArticles();
            return count != null ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    public Long getArchivedArticlesCount() {
        try {
            Long count = supportArticleRepository.countArchivedArticles();
            return count != null ? count : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }

    public Long getTotalViews() {
        try {
            Long views = supportArticleRepository.getTotalViews();
            return views != null ? views : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
}

