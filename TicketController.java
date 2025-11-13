package com.helpdesk.controller;

import com.helpdesk.entity.Reply;
import com.helpdesk.entity.Staff;
import com.helpdesk.entity.Ticket;
import com.helpdesk.entity.User;
import com.helpdesk.service.CategoryService;
import com.helpdesk.service.ReplyService;
import com.helpdesk.service.StaffService;
import com.helpdesk.service.TicketService;
import com.helpdesk.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private ReplyService replyService;

    @Autowired
    private UserService userService;


    // List all tickets with pagination and search
    @GetMapping
    public String listTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long staffId,
            HttpSession session,
            Model model) {

        // Check if business admin is logged in via session (for Business Admin Portal)
        Boolean businessAdminLoggedIn = (Boolean) session.getAttribute("businessAdminLoggedIn");
        
        // If not logged in via session, check Spring Security authentication
        if (businessAdminLoggedIn == null || !businessAdminLoggedIn) {
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                // No session and no Spring Security authentication - redirect to login
                return "redirect:/login";
            }
        }

        Page<Ticket> ticketPage;

        if (search != null && !search.trim().isEmpty()) {
            ticketPage = ticketService.searchTickets(search, page, size, sortBy, sortDir);
        } else if (status != null && !status.isEmpty()) {
            ticketPage = ticketService.getTicketsByStatus(Ticket.Status.valueOf(status), page, size);
        } else if (priority != null && !priority.isEmpty()) {
            ticketPage = ticketService.getTicketsByPriority(Ticket.Priority.valueOf(priority), page, size);
        } else if (categoryId != null) {
            ticketPage = ticketService.getTicketsByCategory(categoryId, page, size);
        } else if (staffId != null) {
            ticketPage = ticketService.getTicketsByAssignedStaff(staffId, page, size);
        } else {
            ticketPage = ticketService.getAllTickets(page, size, sortBy, sortDir);
        }

        model.addAttribute("ticketPage", ticketPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ticketPage.getTotalPages());
        model.addAttribute("totalElements", ticketPage.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("reverseSortDir", sortDir.equals("asc") ? "desc" : "asc");
        model.addAttribute("search", search);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedStaffId", staffId);

        // Add filter options
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("staff", staffService.getActiveStaff());
        model.addAttribute("statuses", Ticket.Status.values());
        model.addAttribute("priorities", Ticket.Priority.values());

        return "tickets/list";
    }

    // Show ticket details (supports both student portal and existing functionality)
    @GetMapping("/{id}")
    public String viewTicket(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // Get current user if authenticated via Spring Security (for Student Portal)
        User currentUser = userService.getCurrentUser();
        
        Optional<Ticket> ticketOpt = ticketService.getTicketById(id);
        if (ticketOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Ticket not found");
            if (currentUser != null && currentUser.getRole() == User.UserRole.STUDENT) {
                return "redirect:/student/tickets";
            }
            return "redirect:/tickets";
        }

        Ticket ticket = ticketOpt.get();

        // Authorization check for students - they can only view their own tickets
        if (currentUser != null && currentUser.getRole() == User.UserRole.STUDENT) {
            if (ticket.getUser() == null || !ticket.getUser().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to view this ticket");
                return "redirect:/student/tickets";
            }
        }

        // Get all replies for this ticket
        List<Reply> replies = replyService.getRepliesByTicket(id);

        model.addAttribute("ticket", ticket);
        model.addAttribute("replies", replies);
        model.addAttribute("newReply", new Reply());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("staff", staffService.getActiveStaff());

        // Route to appropriate view based on user role
        if (currentUser != null && (currentUser.getRole() == User.UserRole.STUDENT || 
                                     currentUser.getRole() == User.UserRole.ADMIN || 
                                     currentUser.getRole() == User.UserRole.STAFF)) {
            return "student/tickets/view"; // Student, Admin, and Staff use the new view with layout
        } else {
            return "tickets/view"; // Existing view for business admin (session-based)
        }
    }

    // Show create ticket form
    @GetMapping("/new")
    public String showCreateForm(HttpSession session, Model model) {
        // Check if business admin is logged in via session (for Business Admin Portal)
        Boolean businessAdminLoggedIn = (Boolean) session.getAttribute("businessAdminLoggedIn");
        
        // If not logged in via session, check Spring Security authentication
        if (businessAdminLoggedIn == null || !businessAdminLoggedIn) {
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                // No session and no Spring Security authentication - redirect to login
                return "redirect:/login";
            }
        }
        
        model.addAttribute("ticket", new Ticket());
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("priorities", Ticket.Priority.values());
        return "tickets/form";
    }

    // Create new ticket
    @PostMapping
    public String createTicket(@Valid @ModelAttribute Ticket ticket,
                               BindingResult result,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("priorities", Ticket.Priority.values());
            return "tickets/form";
        }

        try {
            Ticket savedTicket = ticketService.createTicket(ticket);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Ticket created successfully with ID: " + savedTicket.getId());
            return "redirect:/tickets/" + savedTicket.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error creating ticket: " + e.getMessage());
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("priorities", Ticket.Priority.values());
            return "tickets/form";
        }
    }

    // Show edit ticket form
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Optional<Ticket> ticketOpt = ticketService.getTicketById(id);
        if (ticketOpt.isPresent()) {
            model.addAttribute("ticket", ticketOpt.get());
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("priorities", Ticket.Priority.values());
            model.addAttribute("statuses", Ticket.Status.values());
            model.addAttribute("staff", staffService.getActiveStaff());
            return "tickets/edit";
        }
        return "redirect:/tickets";
    }

    // Update ticket
    @PostMapping("/{id}")
    public String updateTicket(@PathVariable Long id,
                               @Valid @ModelAttribute Ticket ticket,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("priorities", Ticket.Priority.values());
            model.addAttribute("statuses", Ticket.Status.values());
            model.addAttribute("staff", staffService.getActiveStaff());
            return "tickets/edit";
        }

        try {
            ticket.setId(id);
            Ticket updatedTicket = ticketService.updateTicket(ticket);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket updated successfully");
            return "redirect:/tickets/" + updatedTicket.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating ticket: " + e.getMessage());
            model.addAttribute("categories", categoryService.getActiveCategories());
            model.addAttribute("priorities", Ticket.Priority.values());
            model.addAttribute("statuses", Ticket.Status.values());
            model.addAttribute("staff", staffService.getActiveStaff());
            return "tickets/edit";
        }
    }

    // Delete ticket
    @PostMapping("/{id}/delete")
    public String deleteTicket(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ticketService.deleteTicket(id);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting ticket: " + e.getMessage());
        }
        return "redirect:/tickets";
    }

    // Assign ticket to staff
    @PostMapping("/{id}/assign")
    public String assignTicket(@PathVariable Long id,
                               @RequestParam Long staffId,
                               RedirectAttributes redirectAttributes) {
        try {
            Optional<Ticket> ticketOpt = ticketService.getTicketById(id);
            Optional<Staff> staffOpt = staffService.getStaffById(staffId);

            if (ticketOpt.isPresent() && staffOpt.isPresent()) {
                Ticket ticket = ticketOpt.get();
                ticket.setAssignedStaff(staffOpt.get());
                ticket.setStatus(Ticket.Status.IN_PROGRESS);
                ticketService.updateTicket(ticket);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Ticket assigned to " + staffOpt.get().getName());
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error assigning ticket: " + e.getMessage());
        }
        return "redirect:/tickets/" + id;
    }

    // Resolve ticket
    @PostMapping("/{id}/resolve")
    public String resolveTicket(@PathVariable Long id,
                                @RequestParam String resolutionNotes,
                                RedirectAttributes redirectAttributes) {
        try {
            ticketService.resolveTicket(id, resolutionNotes);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket resolved successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error resolving ticket: " + e.getMessage());
        }
        return "redirect:/tickets/" + id;
    }

    // Add reply to ticket (for Student Portal integration)
    @PostMapping("/{id}/reply")
    public String addReply(@PathVariable Long id,
                           @RequestParam String content,
                           RedirectAttributes redirectAttributes) {
        
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Ticket> ticketOpt = ticketService.findById(id);
        if (ticketOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Ticket not found");
            return "redirect:/student/tickets";
        }

        // Validate content
        if (content == null || content.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Reply content cannot be empty");
            return "redirect:/tickets/" + id;
        }

        if (content.trim().length() > 2000) {
            redirectAttributes.addFlashAttribute("error", "Reply content must not exceed 2000 characters");
            return "redirect:/tickets/" + id;
        }

        try {
            Ticket ticket = ticketOpt.get();
            replyService.createReply(content.trim(), ticket, currentUser);

            // Update ticket status if it's OPEN (move to IN_PROGRESS when first reply is added by staff)
            if (ticket.getStatus() == Ticket.Status.OPEN && 
                (currentUser.getRole() == User.UserRole.STAFF || currentUser.getRole() == User.UserRole.ADMIN)) {
                ticket.setStatus(Ticket.Status.IN_PROGRESS);
                ticketService.updateTicket(ticket);
            }

            redirectAttributes.addFlashAttribute("success", "Reply added successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error adding reply: " + e.getMessage());
        }

        return "redirect:/tickets/" + id;
    }
}
