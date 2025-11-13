package com.helpdesk.controller;

import com.helpdesk.entity.ArticleCategory;
import com.helpdesk.service.ArticleCategoryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/support/categories")
public class ArticleCategoryController {

    @Autowired
    private ArticleCategoryService articleCategoryService;

    @GetMapping
    public String showCategoriesPage(HttpSession session, Model model) {
        // Check if user is logged in
        String lecturerUser = (String) session.getAttribute("lecturerUser");
        if (lecturerUser == null) {
            return "redirect:/support/login";
        }

        List<ArticleCategory> categories = articleCategoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("category", new ArticleCategory());
        model.addAttribute("username", lecturerUser);
        return "support/categories/list";
    }

    @PostMapping
    public String createCategory(HttpSession session,
                                 @Valid @ModelAttribute ArticleCategory category,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String lecturerUser = (String) session.getAttribute("lecturerUser");
        if (lecturerUser == null) {
            return "redirect:/support/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("categories", articleCategoryService.getAllCategories());
            model.addAttribute("username", lecturerUser);
            return "support/categories/list";
        }

        try {
            articleCategoryService.createCategory(category);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Category '" + category.getName() + "' created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error creating category: " + e.getMessage());
        }
        return "redirect:/support/categories";
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(HttpSession session, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String lecturerUser = (String) session.getAttribute("lecturerUser");
        if (lecturerUser == null) {
            return "redirect:/support/login";
        }

        try {
            articleCategoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deactivated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deactivating category: " + e.getMessage());
        }
        return "redirect:/support/categories";
    }

    @PostMapping("/{id}/update")
    public String updateCategory(HttpSession session,
                                 @PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam String description,
                                 RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String lecturerUser = (String) session.getAttribute("lecturerUser");
        if (lecturerUser == null) {
            return "redirect:/support/login";
        }

        try {
            ArticleCategory category = articleCategoryService.getCategoryById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            category.setName(name);
            category.setDescription(description);
            articleCategoryService.updateCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating category: " + e.getMessage());
        }
        return "redirect:/support/categories";
    }
}

