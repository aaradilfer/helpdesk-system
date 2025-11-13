package com.helpdesk.controller;

import com.helpdesk.entity.Reply;
import com.helpdesk.entity.Ticket;
import com.helpdesk.entity.User;
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

@Controller
@RequestMapping("/staff")
@PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
public class StaffController {

    @Autowired
    private UserService userService;

    @Autowired
    private TicketService ticketService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Get assigned tickets statistics
        Pageable assignedTicketsPageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        Page<Ticket> assignedTickets = ticketService.findTicketsByAssignedTo(currentUser, assignedTicketsPageable);

        // Get recent tickets that need attention
        Pageable recentTicketsPageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Ticket> recentTickets = ticketService.findTicketsWithFilters(
                Ticket.Status.OPEN, null, null, null, recentTicketsPageable);

        // Get statistics
        long totalTickets = ticketService.getTotalTicketsCount();
        long openTickets = ticketService.getOpenTicketsCount();
        long inProgressTickets = ticketService.getInProgressTicketsCount();
        long resolvedTickets = ticketService.getResolvedTicketsCount();

        model.addAttribute("user", currentUser);
        model.addAttribute("assignedTickets", assignedTickets);
        model.addAttribute("recentTickets", recentTickets);
        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("openTickets", openTickets);
        model.addAttribute("inProgressTickets", inProgressTickets);
        model.addAttribute("resolvedTickets", resolvedTickets);
        model.addAttribute("assignedTicketsCount", assignedTickets.getTotalElements());

        return "staff/dashboard";
    }

    @GetMapping("/tickets")
    public String listTickets(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String filter,
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

        Page<Ticket> tickets;
        if ("assigned".equals(filter)) {
            // Show only tickets assigned to current staff member
            tickets = ticketService.findTicketsByAssignedTo(currentUser, pageable);
        } else {
            // Show all tickets
            tickets = ticketService.findTicketsWithFilters(statusEnum, priorityEnum, null, search, pageable);
        }

        model.addAttribute("user", currentUser);
        model.addAttribute("tickets", tickets);
        model.addAttribute("currentPage", page);
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("priority", priority);
        model.addAttribute("filter", filter);
        model.addAttribute("statuses", Ticket.Status.values());
        model.addAttribute("priorities", Ticket.Priority.values());

        return "staff/tickets";
    }

    @GetMapping("/tickets/{id}")
    public String viewTicket(@PathVariable Long id) {
        // Redirect to the common ticket view endpoint
        return "redirect:/tickets/" + id;
    }

    @PostMapping("/tickets/{id}/reply")
    public String addReply(@PathVariable Long id,
            @Valid @ModelAttribute("newReply") Reply reply,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        // Redirect to the common ticket reply endpoint
        return "forward:/tickets/" + id + "/reply";
    }

    @PostMapping("/tickets/{id}/status")
    public String updateTicketStatus(@PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {
        try {
            Ticket.Status statusEnum = Ticket.Status.valueOf(status);
            ticketService.updateTicketStatus(id, statusEnum);
            redirectAttributes.addFlashAttribute("success", "Ticket status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating ticket status: " + e.getMessage());
        }
        return "redirect:/staff/tickets/" + id;
    }

    @PostMapping("/tickets/{id}/assign-to-me")
    public String assignTicketToMe(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            ticketService.assignTicket(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Ticket assigned to you successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error assigning ticket: " + e.getMessage());
        }
        return "redirect:/staff/tickets/" + id;
    }
}
