package com.helpdesk.controller;

import com.helpdesk.entity.Category;
import com.helpdesk.entity.PaymentTransaction;
import com.helpdesk.service.PaymentTransactionService;
import com.helpdesk.service.PaymentCategoryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

/**
 * Payment Transaction Controller - Handles all payment transaction operations
 * Uses dedicated PaymentTransaction entity (separate from Ticket)
 */
@Controller
@RequestMapping("/payment/transactions")
public class PaymentTransactionController {

    @Autowired
    private PaymentTransactionService paymentTransactionService;

    @Autowired
    private PaymentCategoryService paymentCategoryService;

    private static final String UPLOAD_DIR = "uploads/payment-attachments/";

    private String getCurrentUsername(HttpSession session) {
        String paymentUser = (String) session.getAttribute("paymentUser");
        return paymentUser != null ? paymentUser : "Unknown";
    }

    /**
     * LIST: Show all payment transactions with pagination and filters
     */
    @GetMapping
    public String showTransactionsPage(
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            Model model) {

        // Check if user is logged in
        String paymentUser = (String) session.getAttribute("paymentUser");
        if (paymentUser == null) {
            return "redirect:/payment/login";
        }

        // Get transactions with filters
        Page<PaymentTransaction> transactionsPage = paymentTransactionService.searchWithFilters(
                status, categoryId, null, search, page, size, sortBy, sortDir);

        model.addAttribute("transactions", transactionsPage.getContent());
        model.addAttribute("page", transactionsPage);
        model.addAttribute("categories", paymentCategoryService.getActiveCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", transactionsPage.getTotalPages());
        model.addAttribute("totalItems", transactionsPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("search", search);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("username", paymentUser);

        return "payment/transactions";
    }

    /**
     * CREATE: Show create transaction form
     */
    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model) {
        // Check if user is logged in
        String paymentUser = (String) session.getAttribute("paymentUser");
        if (paymentUser == null) {
            return "redirect:/payment/login";
        }

        model.addAttribute("transaction", new PaymentTransaction());
        model.addAttribute("categories", paymentCategoryService.getActiveCategories());
        model.addAttribute("paymentMethods", PaymentTransaction.PaymentMethod.values());
        model.addAttribute("statuses", PaymentTransaction.Status.values());
        model.addAttribute("username", paymentUser);

        return "payment/transaction-create";
    }

    /**
     * CREATE: Process create transaction form
     */
    @PostMapping("/create")
    public String createTransaction(
            HttpSession session,
            @Valid @ModelAttribute("transaction") PaymentTransaction transaction,
            BindingResult result,
            @RequestParam(value = "attachment", required = false) MultipartFile attachment,
            @RequestParam("categoryId") Long categoryId,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Check if user is logged in
        String paymentUser = (String) session.getAttribute("paymentUser");
        if (paymentUser == null) {
            return "redirect:/payment/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("categories", paymentCategoryService.getActiveCategories());
            model.addAttribute("paymentMethods", PaymentTransaction.PaymentMethod.values());
            model.addAttribute("statuses", PaymentTransaction.Status.values());
            model.addAttribute("username", paymentUser);
            return "payment/transaction-create";
        }

        try {
            String currentUser = getCurrentUsername(session);

            // Set category
            Optional<Category> categoryOpt = paymentCategoryService.getCategoryById(categoryId);
            if (categoryOpt.isPresent()) {
                transaction.setCategory(categoryOpt.get());
            }

            // Handle file upload
            if (attachment != null && !attachment.isEmpty()) {
                String filename = saveAttachment(attachment);
                transaction.setAttachmentFilename(filename);
            }

            // Create transaction
            PaymentTransaction created = paymentTransactionService.createTransaction(transaction, currentUser);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Transaction created successfully! Transaction Number: " + created.getTransactionNumber());
            return "redirect:/payment/transactions/" + created.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating transaction: " + e.getMessage());
            return "redirect:/payment/transactions/create";
        }
    }

    /**
     * VIEW: Show transaction details
     */
    @GetMapping("/{id}")
    public String viewTransaction(HttpSession session,
                                  @PathVariable Long id,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String paymentUser = (String) session.getAttribute("paymentUser");
        if (paymentUser == null) {
            return "redirect:/payment/login";
        }

        Optional<PaymentTransaction> transactionOpt = paymentTransactionService.getTransactionById(id);
        if (transactionOpt.isPresent()) {
            model.addAttribute("transaction", transactionOpt.get());
            model.addAttribute("categories", paymentCategoryService.getActiveCategories());
            model.addAttribute("username", paymentUser);
            return "payment/transaction-view";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Transaction not found");
            return "redirect:/payment/transactions";
        }
    }

    /**
     * EDIT: Show edit transaction form
     */
    @GetMapping("/{id}/edit")
    public String editTransaction(HttpSession session,
                                  @PathVariable Long id,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String paymentUser = (String) session.getAttribute("paymentUser");
        if (paymentUser == null) {
            return "redirect:/payment/login";
        }

        Optional<PaymentTransaction> transactionOpt = paymentTransactionService.getTransactionById(id);
        if (transactionOpt.isPresent()) {
            model.addAttribute("transaction", transactionOpt.get());
            model.addAttribute("categories", paymentCategoryService.getActiveCategories());
            model.addAttribute("paymentMethods", PaymentTransaction.PaymentMethod.values());
            model.addAttribute("statuses", PaymentTransaction.Status.values());
            model.addAttribute("username", paymentUser);
            return "payment/transaction-edit";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Transaction not found");
            return "redirect:/payment/transactions";
        }
    }

    /**
     * UPDATE: Process edit transaction form
     */
    @PostMapping("/{id}/edit")
    public String updateTransaction(HttpSession session,
                                    @PathVariable Long id,
                                    @ModelAttribute PaymentTransaction transaction,
                                    @RequestParam("categoryId") Long categoryId,
                                    @RequestParam(value = "attachment", required = false) MultipartFile attachment,
                                    RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String paymentUser = (String) session.getAttribute("paymentUser");
        if (paymentUser == null) {
            return "redirect:/payment/login";
        }

        try {
            String currentUser = getCurrentUsername(session);

            // Set category
            Optional<Category> categoryOpt = paymentCategoryService.getCategoryById(categoryId);
            if (categoryOpt.isPresent()) {
                transaction.setCategory(categoryOpt.get());
            }

            // Handle file upload
            if (attachment != null && !attachment.isEmpty()) {
                String filename = saveAttachment(attachment);
                transaction.setAttachmentFilename(filename);
            }

            // Update transaction
            paymentTransactionService.updateTransaction(id, transaction, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction updated successfully");
            return "redirect:/payment/transactions/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating transaction: " + e.getMessage());
            return "redirect:/payment/transactions/" + id + "/edit";
        }
    }

    /**
     * VERIFY: Verify/Reject transaction
     */
    @PostMapping("/{id}/verify")
    public String verifyTransaction(HttpSession session,
                                   @PathVariable Long id,
                                   @RequestParam Boolean verified,
                                   RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String paymentUser = (String) session.getAttribute("paymentUser");
        if (paymentUser == null) {
            return "redirect:/payment/login";
        }

        try {
            String currentUser = getCurrentUsername(session);
            paymentTransactionService.verifyTransaction(id, verified, currentUser);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Transaction " + (verified ? "verified" : "rejected") + " successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error verifying transaction: " + e.getMessage());
        }
        return "redirect:/payment/transactions/" + id;
    }

    /**
     * STATUS: Update transaction status
     */
    @PostMapping("/{id}/status")
    public String updateTransactionStatus(HttpSession session,
                                         @PathVariable Long id,
                                         @RequestParam String status,
                                         RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String paymentUser = (String) session.getAttribute("paymentUser");
        if (paymentUser == null) {
            return "redirect:/payment/login";
        }

        try {
            String currentUser = getCurrentUsername(session);
            PaymentTransaction.Status statusEnum = PaymentTransaction.Status.valueOf(status.toUpperCase());
            paymentTransactionService.updateStatus(id, statusEnum, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction status updated to " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating status: " + e.getMessage());
        }
        return "redirect:/payment/transactions/" + id;
    }

    /**
     * DELETE: Delete transaction
     */
    @PostMapping("/{id}/delete")
    public String deleteTransaction(HttpSession session,
                                    @PathVariable Long id,
                                    RedirectAttributes redirectAttributes) {
        // Check if user is logged in
        String paymentUser = (String) session.getAttribute("paymentUser");
        if (paymentUser == null) {
            return "redirect:/payment/login";
        }

        try {
            paymentTransactionService.deleteTransaction(id);
            redirectAttributes.addFlashAttribute("successMessage", "Transaction deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting transaction: " + e.getMessage());
        }
        return "redirect:/payment/transactions";
    }

    /**
     * HELPER: Save uploaded attachment file
     */
    private String saveAttachment(MultipartFile file) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
        String uniqueFilename = UUID.randomUUID().toString() + extension;

        // Save file
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath);

        return uniqueFilename;
    }
}

