package com.helpdesk.controller;

import com.helpdesk.dto.DashboardStatsDTO;
import com.helpdesk.service.TicketService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DashboardController {

    @Autowired
    private TicketService ticketService;

    // Business Admin Dashboard page
    @GetMapping("/business-admin/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // Check if business admin is logged in via session
        Boolean businessAdminLoggedIn = (Boolean) session.getAttribute("businessAdminLoggedIn");

        if (businessAdminLoggedIn == null || !businessAdminLoggedIn) {
            // If not logged in via session, check Spring Security authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return "redirect:/business-admin/login?error=access_denied";
            }
        }

        DashboardStatsDTO stats = ticketService.getDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("isBusinessAdmin", businessAdminLoggedIn != null && businessAdminLoggedIn);

        return "dashboard/index";
    }

    // API endpoint for Business Admin dashboard stats (for AJAX updates)
    @GetMapping("/business-admin/api/dashboard/stats")
    @ResponseBody
    public DashboardStatsDTO getDashboardStats(HttpSession session) {
        // Optional: Add session validation for API calls
        Boolean businessAdminLoggedIn = (Boolean) session.getAttribute("businessAdminLoggedIn");
        if (businessAdminLoggedIn == null || !businessAdminLoggedIn) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                throw new AccessDeniedException("Access denied");
            }
        }

        return ticketService.getDashboardStats();
    }
}