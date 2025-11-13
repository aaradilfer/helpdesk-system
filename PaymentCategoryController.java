package com.helpdesk.controller;

import com.helpdesk.entity.Category;
import com.helpdesk.service.PaymentCategoryService;
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
@RequestMapping("/payment/categories")
public class PaymentCategoryController {

    @Autowired
    private PaymentCategoryService paymentCategoryService;

    @GetMapping
    public String showCategoriesPage(HttpSession session, Model model) {
        // Check if user is logged in
        String paymentUser = (String) session.getAttribute("paymentUser");
        if (paymentUser == null) {
            return "redirect:/payment/login";
        }

        List<Category> categories = paymentCategoryService.getActiveCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("category", new Category());
        model.addAttribute("username", paymentUser);
        return "payment/categories";
    }

    @PostMapping
    public String createCategory(HttpSession session,
                                 @Valid @ModelAttribute Category category,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String paymentUser = (String) session.getAttribute("paymentUser");
        if (paymentUser == null) {
            return "redirect:/payment/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("categories", paymentCategoryService.getActiveCategories());
            model.addAttribute("username", paymentUser);
            return "payment/categories";
        }

        try {
            paymentCategoryService.createCategory(category);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Category '" + category.getName() + "' created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error creating category: " + e.getMessage());
        }
        return "redirect:/payment/categories";
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(HttpSession session,
                                 @PathVariable Long id,
                                 RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String paymentUser = (String) session.getAttribute("paymentUser");
        if (paymentUser == null) {
            return "redirect:/payment/login";
        }

        try {
            paymentCategoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting category: " + e.getMessage());
        }
        return "redirect:/payment/categories";
    }

    @PostMapping("/{id}/update")
    public String updateCategory(HttpSession session,
                                 @PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam String description,
                                 RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String paymentUser = (String) session.getAttribute("paymentUser");
        if (paymentUser == null) {
            return "redirect:/payment/login";
        }

        try {
            Category category = paymentCategoryService.getCategoryById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            category.setName(name);
            category.setDescription(description);
            paymentCategoryService.updateCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating category: " + e.getMessage());
        }
        return "redirect:/payment/categories";
    }
}

