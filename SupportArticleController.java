package com.helpdesk.controller;

import com.helpdesk.entity.SupportArticle;
import com.helpdesk.service.SupportArticleService;
import com.helpdesk.service.ArticleCategoryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/support/articles")
public class SupportArticleController {

    @Autowired
    private SupportArticleService supportArticleService;

    @Autowired
    private ArticleCategoryService articleCategoryService;

    @GetMapping
    public String showArticlesPage(HttpSession session, Model model) {
        // Check if user is logged in
        String lecturerUser = (String) session.getAttribute("lecturerUser");
        if (lecturerUser == null) {
            return "redirect:/support/login";
        }

        List<SupportArticle> articles = supportArticleService.getAllArticles();
        model.addAttribute("articles", articles);
        model.addAttribute("categories", articleCategoryService.getActiveCategories());
        model.addAttribute("username", lecturerUser);
        return "support/articles/list";
    }

    @GetMapping("/create")
    public String showCreateArticleForm(HttpSession session, Model model) {
        // Check if user is logged in
        String lecturerUser = (String) session.getAttribute("lecturerUser");
        if (lecturerUser == null) {
            return "redirect:/support/login";
        }

        model.addAttribute("article", new SupportArticle());
        model.addAttribute("categories", articleCategoryService.getActiveCategories());
        model.addAttribute("username", lecturerUser);
        return "support/articles/create";
    }

    @PostMapping
    public String createArticle(HttpSession session,
                                @Valid @ModelAttribute SupportArticle article,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String lecturerUser = (String) session.getAttribute("lecturerUser");
        if (lecturerUser == null) {
            return "redirect:/support/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("categories", articleCategoryService.getActiveCategories());
            model.addAttribute("username", lecturerUser);
            return "support/articles/create";
        }

        try {
            article.setCreatedAt(LocalDateTime.now());
            article.setUpdatedAt(LocalDateTime.now());
            article.setAuthor(lecturerUser);
            supportArticleService.createArticle(article);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Article '" + article.getTitle() + "' created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error creating article: " + e.getMessage());
        }
        return "redirect:/support/articles";
    }

    @GetMapping("/{id}")
    public String viewArticle(HttpSession session, @PathVariable Long id, Model model) {
        // Check if user is logged in
        String lecturerUser = (String) session.getAttribute("lecturerUser");
        if (lecturerUser == null) {
            return "redirect:/support/login";
        }

        SupportArticle article = supportArticleService.getArticleById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        model.addAttribute("article", article);
        model.addAttribute("username", lecturerUser);
        return "support/articles/view";
    }

    @GetMapping("/{id}/edit")
    public String editArticle(HttpSession session, @PathVariable Long id, Model model) {
        // Check if user is logged in
        String lecturerUser = (String) session.getAttribute("lecturerUser");
        if (lecturerUser == null) {
            return "redirect:/support/login";
        }

        SupportArticle article = supportArticleService.getArticleById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        model.addAttribute("article", article);
        model.addAttribute("categories", articleCategoryService.getActiveCategories());
        model.addAttribute("username", lecturerUser);
        return "support/articles/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateArticle(HttpSession session,
                                @PathVariable Long id,
                                @Valid @ModelAttribute SupportArticle article,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String lecturerUser = (String) session.getAttribute("lecturerUser");
        if (lecturerUser == null) {
            return "redirect:/support/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("categories", articleCategoryService.getActiveCategories());
            model.addAttribute("username", lecturerUser);
            return "support/articles/edit";
        }

        try {
            SupportArticle existingArticle = supportArticleService.getArticleById(id)
                    .orElseThrow(() -> new RuntimeException("Article not found"));

            existingArticle.setTitle(article.getTitle());
            existingArticle.setContent(article.getContent());
            existingArticle.setCategory(article.getCategory());
            existingArticle.setKeywords(article.getKeywords());
            existingArticle.setStatus(article.getStatus());
            existingArticle.setUpdatedAt(LocalDateTime.now());
            existingArticle.setLastModifiedBy(lecturerUser);

            supportArticleService.updateArticle(existingArticle);
            redirectAttributes.addFlashAttribute("successMessage", "Article updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating article: " + e.getMessage());
        }
        return "redirect:/support/articles";
    }

    @PostMapping("/{id}/delete")
    public String deleteArticle(HttpSession session, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String lecturerUser = (String) session.getAttribute("lecturerUser");
        if (lecturerUser == null) {
            return "redirect:/support/login";
        }

        try {
            supportArticleService.deleteArticle(id);
            redirectAttributes.addFlashAttribute("successMessage", "Article deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting article: " + e.getMessage());
        }
        return "redirect:/support/articles";
    }

    @PostMapping("/{id}/publish")
    public String publishArticle(HttpSession session, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String lecturerUser = (String) session.getAttribute("lecturerUser");
        if (lecturerUser == null) {
            return "redirect:/support/login";
        }

        try {
            supportArticleService.updateArticleStatus(id, "Published", lecturerUser);
            redirectAttributes.addFlashAttribute("successMessage", "Article published successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error publishing article: " + e.getMessage());
        }
        return "redirect:/support/articles";
    }

    @PostMapping("/{id}/archive")
    public String archiveArticle(HttpSession session, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String lecturerUser = (String) session.getAttribute("lecturerUser");
        if (lecturerUser == null) {
            return "redirect:/support/login";
        }

        try {
            supportArticleService.updateArticleStatus(id, "Archived", lecturerUser);
            redirectAttributes.addFlashAttribute("successMessage", "Article archived successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error archiving article: " + e.getMessage());
        }
        return "redirect:/support/articles";
    }

    // Public endpoints for knowledge base (NO SESSION CHECK - accessible to everyone)
    @GetMapping("/public")
    public String publicKnowledgeBase(@RequestParam(required = false) String search, Model model) {
        List<SupportArticle> articles;
        if (search != null && !search.trim().isEmpty()) {
            articles = supportArticleService.searchArticles(search);
        } else {
            articles = supportArticleService.getPublishedArticles();
        }
        model.addAttribute("articles", articles);
        model.addAttribute("categories", articleCategoryService.getActiveCategories());
        model.addAttribute("searchQuery", search);
        return "support/articles/public-list";
    }

    @GetMapping("/public/{id}")
    public String viewPublicArticle(@PathVariable Long id, Model model) {
        SupportArticle article = supportArticleService.getArticleById(id)
                .orElseThrow(() -> new RuntimeException("Article not found"));
        model.addAttribute("article", article);
        return "support/articles/public-view";
    }

    @PostMapping("/public/{id}/feedback")
    public String submitFeedback(@PathVariable Long id,
                                 @RequestParam boolean helpful,
                                 RedirectAttributes redirectAttributes) {
        try {
            supportArticleService.recordFeedback(id, helpful);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Thank you for your feedback!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error submitting feedback: " + e.getMessage());
        }
        return "redirect:/support/articles/public/" + id;
    }
}

