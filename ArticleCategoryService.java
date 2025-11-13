package com.helpdesk.service;

import com.helpdesk.entity.ArticleCategory;
import com.helpdesk.repository.ArticleCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ArticleCategoryService {

    @Autowired
    private ArticleCategoryRepository articleCategoryRepository;

    public ArticleCategory createCategory(ArticleCategory category) {
        if (articleCategoryRepository.countByName(category.getName()) > 0) {
            throw new RuntimeException("Category with name '" + category.getName() + "' already exists");
        }
        return articleCategoryRepository.save(category);
    }

    public Optional<ArticleCategory> getCategoryById(Long id) {
        return articleCategoryRepository.findById(id);
    }

    public List<ArticleCategory> getAllCategories() {
        return articleCategoryRepository.findAll();
    }

    public List<ArticleCategory> getActiveCategories() {
        return articleCategoryRepository.findByIsActiveTrue();
    }

    public ArticleCategory updateCategory(ArticleCategory category) {
        return articleCategoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        Optional<ArticleCategory> categoryOpt = articleCategoryRepository.findById(id);
        if (categoryOpt.isPresent()) {
            ArticleCategory category = categoryOpt.get();
            category.setIsActive(false);
            articleCategoryRepository.save(category);
        }
    }

    public Optional<ArticleCategory> getCategoryByName(String name) {
        return articleCategoryRepository.findByName(name);
    }
}

