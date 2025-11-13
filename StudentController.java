package com.helpdesk.controller;

import com.helpdesk.entity.Category;
import com.helpdesk.entity.Ticket;
import com.helpdesk.entity.User;
import com.helpdesk.service.CategoryService;
import com.helpdesk.service.TicketService;
import com.helpdesk.service.UserService;
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

import java.util.Optional;

@Controller
@RequestMapping("/student")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    @Autowired
    private UserService userService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Get user's ticket statistics
        Pageable recentTicketsPageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        Page<Ticket> recentTickets = ticketService.findTicketsByUser(currentUser, recentTicketsPageable);

        // Count tickets by status for current user
        long openTicketsCount = ticketService.countTicketsByUserAndStatus(currentUser, Ticket.Status.OPEN);
        long inProgressTicketsCount = ticketService.countTicketsByUserAndStatus(currentUser, Ticket.Status.IN_PROGRESS);
        long resolvedTicketsCount = ticketService.countTicketsByUserAndStatus(currentUser, Ticket.Status.RESOLVED);
        long totalTicketsCount = ticketService.countTicketsByUser(currentUser);

        model.addAttribute("user", currentUser);
        model.addAttribute("recentTickets", recentTickets);
        model.addAttribute("openTicketsCount", openTicketsCount);
        model.addAttribute("inProgressTicketsCount", inProgressTicketsCount);
        model.addAttribute("resolvedTicketsCount", resolvedTicketsCount);
        model.addAttribute("totalTicketsCount", totalTicketsCount);

        return "student/dashboard";
    }

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

        Ticket.Status statusEnum = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusEnum = Ticket.Status.valueOf(status);
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore
            }
        }

        Ticket.Priority priorityEnum = null;
        if (priority != null && !priority.isEmpty()) {
            try {
                priorityEnum = Ticket.Priority.valueOf(priority);
            } catch (IllegalArgumentException e) {
                // Invalid priority, ignore
            }
        }

        Page<Ticket> tickets = ticketService.findUserTicketsWithFilters(
                currentUser, statusEnum, priorityEnum, categoryId, search, pageable);

        model.addAttribute("tickets", tickets);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("currentPage", page);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("priority", priority);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("statuses", Ticket.Status.values());
        model.addAttribute("priorities", Ticket.Priority.values());

        return "student/tickets";
    }

    @GetMapping("/tickets/new")
    public String showCreateTicketForm(Model model) {
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("priorities", Ticket.Priority.values());
        return "student/create-ticket";
    }

    @PostMapping("/tickets/new")
    public String createTicket(@ModelAttribute("ticket") Ticket ticket,
            BindingResult result,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            Model model,
            RedirectAttributes redirectAttributes) {

        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        System.out.println("=== CREATE TICKET DEBUG ===");
        System.out.println("Ticket Title: " + ticket.getTitle());
        System.out.println("Category ID param: " + categoryId);

        // Set category BEFORE validation
        if (categoryId != null && categoryId > 0) {
            Optional<Category> categoryOpt = categoryService.getCategoryById(categoryId);
            if (categoryOpt.isPresent()) {
                ticket.setCategory(categoryOpt.get());
            }
        }

        // Manual validation
        if (ticket.getTitle() == null || ticket.getTitle().trim().isEmpty()) {
            result.rejectValue("title", "error.ticket", "Title is required");
        } else if (ticket.getTitle().length() < 5 || ticket.getTitle().length() > 200) {
            result.rejectValue("title", "error.ticket", "Title must be between 5 and 200 characters");
        }

        if (ticket.getDescription() == null || ticket.getDescription().trim().isEmpty()) {
            result.rejectValue("description", "error.ticket", "Description is required");
        } else if (ticket.getDescription().length() < 10 || ticket.getDescription().length() > 2000) {
            result.rejectValue("description", "error.ticket", "Description must be between 10 and 2000 characters");
        }

        if (ticket.getPriority() == null) {
            result.rejectValue("priority", "error.ticket", "Priority is required");
        }

        if (ticket.getCategory() == null) {
            result.rejectValue("category", "error.ticket", "Category is required");
        }

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("priorities", Ticket.Priority.values());
            return "student/create-ticket";
        }

        try {
            // Set the user who created the ticket
            ticket.setUser(currentUser);
            
            // Set legacy fields for compatibility with existing system
            ticket.setStudentName(currentUser.getFullName());
            ticket.setStudentEmail(currentUser.getEmail());
            ticket.setStudentId(currentUser.getUsername());
            ticket.setStudentPhone(currentUser.getPhoneNumber());

            Ticket createdTicket = ticketService.createTicket(ticket);
            System.out.println("Ticket created successfully with ID: " + createdTicket.getId());
            redirectAttributes.addFlashAttribute("success", "Ticket created successfully!");
            return "redirect:/student/tickets";
        } catch (Exception e) {
            System.out.println("Error creating ticket: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error creating ticket: " + e.getMessage());
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("priorities", Ticket.Priority.values());
            return "student/create-ticket";
        }
    }

    @GetMapping("/tickets/{id}")
    public String viewTicket(@PathVariable Long id) {
        // Redirect to the common ticket view endpoint
        return "redirect:/tickets/" + id;
    }

    @GetMapping("/tickets/{id}/edit")
    public String showEditTicketForm(@PathVariable Long id, Model model) {
        Optional<Ticket> ticketOpt = ticketService.findById(id);
        User currentUser = userService.getCurrentUser();

        if (ticketOpt.isEmpty() || currentUser == null) {
            return "redirect:/student/tickets";
        }

        Ticket ticket = ticketOpt.get();

        // Check if user can edit this ticket
        if (!ticketService.canUserEditTicket(ticket, currentUser)) {
            return "redirect:/student/tickets/" + id;
        }

        model.addAttribute("ticket", ticket);
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("priorities", Ticket.Priority.values());

        return "student/edit-ticket";
    }

    @PostMapping("/tickets/{id}/edit")
    public String updateTicket(@PathVariable Long id,
            @RequestParam Long categoryId,
            @ModelAttribute("ticket") Ticket ticket,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        User currentUser = userService.getCurrentUser();
        Optional<Ticket> existingTicketOpt = ticketService.findById(id);

        if (existingTicketOpt.isEmpty() || currentUser == null) {
            return "redirect:/student/tickets";
        }

        Ticket existingTicket = existingTicketOpt.get();

        if (!ticketService.canUserEditTicket(existingTicket, currentUser)) {
            redirectAttributes.addFlashAttribute("error", "You cannot edit this ticket.");
            return "redirect:/student/tickets/" + id;
        }

        // Get and set category
        Optional<Category> categoryOpt = categoryService.getCategoryById(categoryId);
        if (categoryOpt.isEmpty()) {
            model.addAttribute("ticket", existingTicket);
            model.addAttribute("error", "Invalid category selected.");
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("priorities", Ticket.Priority.values());
            return "student/edit-ticket";
        }

        // Manual validation for required fields
        if (ticket.getTitle() == null || ticket.getTitle().trim().length() < 5
                || ticket.getTitle().trim().length() > 200) {
            model.addAttribute("ticket", existingTicket);
            model.addAttribute("error", "Title must be between 5 and 200 characters.");
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("priorities", Ticket.Priority.values());
            return "student/edit-ticket";
        }

        if (ticket.getDescription() == null || ticket.getDescription().trim().length() < 10
                || ticket.getDescription().trim().length() > 2000) {
            model.addAttribute("ticket", existingTicket);
            model.addAttribute("error", "Description must be between 10 and 2000 characters.");
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("priorities", Ticket.Priority.values());
            return "student/edit-ticket";
        }

        if (ticket.getPriority() == null) {
            model.addAttribute("ticket", existingTicket);
            model.addAttribute("error", "Priority is required.");
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("priorities", Ticket.Priority.values());
            return "student/edit-ticket";
        }

        try {
            // Update only allowed fields
            existingTicket.setTitle(ticket.getTitle().trim());
            existingTicket.setDescription(ticket.getDescription().trim());
            existingTicket.setPriority(ticket.getPriority());
            existingTicket.setCategory(categoryOpt.get());
            existingTicket.setContactEmail(ticket.getContactEmail());
            existingTicket.setContactPhone(ticket.getContactPhone());
            existingTicket.setUrgencyLevel(ticket.getUrgencyLevel());

            ticketService.updateTicket(existingTicket);
            redirectAttributes.addFlashAttribute("success", "Ticket updated successfully!");
            return "redirect:/tickets/" + id;
        } catch (Exception e) {
            model.addAttribute("ticket", existingTicket);
            model.addAttribute("error", "Error updating ticket: " + e.getMessage());
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("priorities", Ticket.Priority.values());
            return "student/edit-ticket";
        }
    }

    @PostMapping("/tickets/{id}/delete")
    public String deleteTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        Optional<Ticket> ticketOpt = ticketService.findById(id);

        if (ticketOpt.isEmpty() || currentUser == null) {
            return "redirect:/student/tickets";
        }

        Ticket ticket = ticketOpt.get();

        if (!ticketService.canUserDeleteTicket(ticket, currentUser)) {
            redirectAttributes.addFlashAttribute("error", "You cannot delete this ticket.");
            return "redirect:/student/tickets";
        }

        try {
            ticketService.deleteTicket(id);
            redirectAttributes.addFlashAttribute("success", "Ticket deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting ticket: " + e.getMessage());
        }

        return "redirect:/student/tickets";
    }
}

