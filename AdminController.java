package com.helpdesk.controller;

import com.helpdesk.entity.Category;
import com.helpdesk.entity.Ticket;
import com.helpdesk.entity.User;
import com.helpdesk.service.CategoryService;
import com.helpdesk.service.TicketService;
import com.helpdesk.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Admin Controller for Admin Portal
 * Handles admin dashboard, user management, ticket oversight, and category management
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Admin Dashboard - Shows statistics and overview
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Get ticket statistics
        long totalTickets = ticketService.getTotalTicketsCount();
        long openTickets = ticketService.getOpenTicketsCount();
        long inProgressTickets = ticketService.getInProgressTicketsCount();
        long resolvedTickets = ticketService.getResolvedTicketsCount();
        long closedTickets = ticketService.getClosedTicketsCount();
        long todayTickets = ticketService.getTicketsCreatedToday();
        long weekTickets = ticketService.getTicketsCreatedThisWeek();

        // Get tickets by category
        List<Object[]> ticketsByCategory = ticketService.getTicketsByCategory();

        // Get tickets by priority
        List<Object[]> ticketsByPriority = ticketService.getTicketsByPriority();

        // Get top 5 students by ticket count
        List<Object[]> topStudents = userService.getTop5StudentsByTicketCount();

        // Get recent tickets
        Pageable recentTicketsPageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Ticket> recentTickets = ticketService.findAllTickets(recentTicketsPageable);

        model.addAttribute("user", currentUser);
        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("openTickets", openTickets);
        model.addAttribute("inProgressTickets", inProgressTickets);
        model.addAttribute("resolvedTickets", resolvedTickets);
        model.addAttribute("closedTickets", closedTickets);
        model.addAttribute("todayTickets", todayTickets);
        model.addAttribute("weekTickets", weekTickets);
        model.addAttribute("ticketsByCategory", ticketsByCategory);
        model.addAttribute("ticketsByPriority", ticketsByPriority);
        model.addAttribute("topStudents", topStudents);
        model.addAttribute("recentTickets", recentTickets);

        return "admin/dashboard";
    }

    /**
     * List all users with filtering
     */
    @GetMapping("/users")
    public String listUsers(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            Model model) {

        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        User.Role roleEnum = null;
        if (role != null && !role.isEmpty()) {
            try {
                roleEnum = User.Role.valueOf(role);
            } catch (IllegalArgumentException e) {
                // Invalid role, ignore
            }
        }

        Page<User> users;
        if (search != null && !search.isEmpty()) {
            users = userService.searchUsers(search, pageable);
        } else if (roleEnum != null) {
            users = userService.findUsersByRole(roleEnum, pageable);
        } else {
            users = userService.findAllUsers(pageable);
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        model.addAttribute("search", search);
        model.addAttribute("role", role);
        model.addAttribute("roles", User.Role.values());

        return "admin/users";
    }

    /**
     * Show create staff form
     */
    @GetMapping("/users/new-staff")
    public String showCreateStaffForm(Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", new User());
        return "admin/create-staff";
    }

    /**
     * Create new staff user
     */
    @PostMapping("/users/new-staff")
    public String createStaff(@ModelAttribute("user") User user,
            @RequestParam String password,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            userService.createStaffUser(user, password);
            redirectAttributes.addFlashAttribute("success", "Staff user created successfully!");
            return "redirect:/admin/users";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", user);
            return "admin/create-staff";
        }
    }

    /**
     * Toggle user status (enable/disable)
     */
    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("success", "User status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating user status: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * List all categories
     */
    @GetMapping("/categories")
    public String listCategories(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String search,
                                Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Category> categories = categoryService.findAllCategories(pageable);

        model.addAttribute("user", currentUser);
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", page);
        model.addAttribute("search", search);

        return "admin/categories";
    }

    /**
     * Show create category form
     */
    @GetMapping("/categories/new")
    public String showCreateCategoryForm(Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("category", new Category());
        return "admin/create-category";
    }

    /**
     * Create new category
     */
    @PostMapping("/categories/new")
    public String createCategory(@Valid @ModelAttribute("category") Category category,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "admin/create-category";
        }

        try {
            categoryService.createCategory(category);
            redirectAttributes.addFlashAttribute("success", "Category created successfully!");
            return "redirect:/admin/categories";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/create-category";
        }
    }

    /**
     * Show edit category form
     */
    @GetMapping("/categories/{id}/edit")
    public String showEditCategoryForm(@PathVariable Long id, Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Category> categoryOpt = categoryService.getCategoryById(id);
        if (categoryOpt.isEmpty()) {
            return "redirect:/admin/categories";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("category", categoryOpt.get());
        return "admin/edit-category";
    }

    /**
     * Update category
     */
    @PostMapping("/categories/{id}/edit")
    public String updateCategory(@PathVariable Long id,
            @Valid @ModelAttribute("category") Category category,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "admin/edit-category";
        }

        try {
            category.setId(id);
            categoryService.updateCategory(category);
            redirectAttributes.addFlashAttribute("success", "Category updated successfully!");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            model.addAttribute("error", "Error updating category: " + e.getMessage());
            return "admin/edit-category";
        }
    }

    /**
     * Toggle category status (active/inactive)
     */
    @PostMapping("/categories/{id}/toggle-status")
    public String toggleCategoryStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.toggleCategoryStatus(id);
            redirectAttributes.addFlashAttribute("success", "Category status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating category status: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    /**
     * List all tickets for admin oversight
     */
    @GetMapping("/tickets")
    public String listTickets(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long categoryId,
            Model model) {

        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Ticket> tickets = ticketService.findAllTickets(pageable);

        model.addAttribute("user", currentUser);
        model.addAttribute("tickets", tickets);
        model.addAttribute("currentPage", page);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("priority", priority);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("statuses", Ticket.Status.values());
        model.addAttribute("priorities", Ticket.Priority.values());

        return "admin/tickets";
    }

    /**
     * Show edit ticket form (admin can edit any ticket)
     */
    @GetMapping("/tickets/{id}/edit")
    public String showEditTicketForm(@PathVariable Long id, Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Ticket> ticketOpt = ticketService.getTicketById(id);
        if (ticketOpt.isEmpty()) {
            return "redirect:/admin/tickets";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("ticket", ticketOpt.get());
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("priorities", Ticket.Priority.values());
        model.addAttribute("statuses", Ticket.Status.values());
        model.addAttribute("users", userService.getAllStaff());

        return "admin/edit-ticket";
    }

    /**
     * Update ticket (admin can update any ticket)
     */
    @PostMapping("/tickets/{id}/edit")
    public String updateTicket(@PathVariable Long id,
            @ModelAttribute("ticket") Ticket ticket,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<Ticket> existingTicketOpt = ticketService.getTicketById(id);
            if (existingTicketOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Ticket not found");
                return "redirect:/admin/tickets";
            }

            Ticket existingTicket = existingTicketOpt.get();
            existingTicket.setTitle(ticket.getTitle());
            existingTicket.setDescription(ticket.getDescription());
            existingTicket.setPriority(ticket.getPriority());
            existingTicket.setStatus(ticket.getStatus());
            existingTicket.setCategory(ticket.getCategory());

            ticketService.updateTicket(existingTicket);
            redirectAttributes.addFlashAttribute("success", "Ticket updated successfully!");
            return "redirect:/tickets/" + id;
        } catch (Exception e) {
            model.addAttribute("error", "Error updating ticket: " + e.getMessage());
            model.addAttribute("ticket", ticket);
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("priorities", Ticket.Priority.values());
            model.addAttribute("statuses", Ticket.Status.values());
            return "admin/edit-ticket";
        }
    }

    /**
     * Update ticket status
     */
    @PostMapping("/tickets/{id}/update-status")
    public String updateTicketStatus(@PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<Ticket> ticketOpt = ticketService.getTicketById(id);
            if (ticketOpt.isPresent()) {
                Ticket ticket = ticketOpt.get();
                ticket.setStatus(Ticket.Status.valueOf(status));
                ticketService.updateTicket(ticket);
                redirectAttributes.addFlashAttribute("success", "Ticket status updated successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating ticket status: " + e.getMessage());
        }
        return "redirect:/admin/tickets";
    }

    /**
     * Delete ticket (Admin only)
     */
    @PostMapping("/tickets/{id}/delete")
    public String deleteTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Ticket> ticketOpt = ticketService.getTicketById(id);
            if (ticketOpt.isPresent()) {
                ticketService.deleteTicket(id);
                redirectAttributes.addFlashAttribute("success", "Ticket deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Ticket not found!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting ticket: " + e.getMessage());
        }
        return "redirect:/admin/tickets";
    }
}

