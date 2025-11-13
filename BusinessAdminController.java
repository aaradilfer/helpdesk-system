package com.helpdesk.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/business-admin")
public class BusinessAdminController {

    @Value("${business.admin.username:admin}")
    private String adminUsername;

    @Value("${business.admin.password:admin123}")
    private String adminPassword;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            if ("session_expired".equals(error)) {
                model.addAttribute("errorMessage", "Session expired. Please login again.");
            } else if ("access_denied".equals(error)) {
                model.addAttribute("errorMessage", "Access denied. Please login.");
            } else {
                model.addAttribute("errorMessage", "Invalid username or password. Please try again.");
            }
        }

        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully");
        }

        return "business-admin/login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model) {

        // Simple authentication check
        if (adminUsername.equals(username) && adminPassword.equals(password)) {
            // Set session attribute to indicate business admin is logged in
            session.setAttribute("businessAdminLoggedIn", true);
            session.setAttribute("businessAdminUsername", username);
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            // Redirect to business admin dashboard on successful login
            return "redirect:/business-admin/dashboard";
        } else {
            // Redirect back to login page with error
            return "redirect:/business-admin/login?error=true";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        // Clear business admin session
        session.removeAttribute("businessAdminLoggedIn");
        session.removeAttribute("businessAdminUsername");
        session.invalidate();

        // Redirect to home page
        return "redirect:/?logout=true";
    }

    /**
     * Business Admin Tickets List - redirects to main tickets page
     * with session validation
     */
    @GetMapping("/tickets")
    public String businessAdminTickets(HttpSession session) {
        // Check if business admin is logged in
        Boolean businessAdminLoggedIn = (Boolean) session.getAttribute("businessAdminLoggedIn");
        
        if (businessAdminLoggedIn == null || !businessAdminLoggedIn) {
            return "redirect:/business-admin/login?error=session_expired";
        }
        
        // Redirect to main tickets page (which is permitted in SecurityConfig)
        return "redirect:/tickets";
    }

    /**
     * Business Admin New Ticket - redirects to main new ticket page
     * with session validation
     */
    @GetMapping("/tickets/new")
    public String businessAdminNewTicket(HttpSession session) {
        // Check if business admin is logged in
        Boolean businessAdminLoggedIn = (Boolean) session.getAttribute("businessAdminLoggedIn");
        
        if (businessAdminLoggedIn == null || !businessAdminLoggedIn) {
            return "redirect:/business-admin/login?error=session_expired";
        }
        
        // Redirect to main new ticket page (which is permitted in SecurityConfig)
        return "redirect:/tickets/new";
    }
}