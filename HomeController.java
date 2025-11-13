package com.helpdesk.controller;

import com.helpdesk.entity.User;
import com.helpdesk.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    /**
     * Index/Home Page
     */
    @GetMapping({"/", "/index", "/home"})
    public String index() {
        return "index";
    }

    /**
     * Student/Admin Login Page
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }

        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }

        return "student/login";
    }

    /**
     * Student Registration Page
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "student/register";
    }

    /**
     * Handle Student Registration
     */
    @PostMapping("/register")
    public String registerStudent(@ModelAttribute("user") User user,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        System.out.println("=== STUDENT REGISTRATION ===");
        System.out.println("Username: " + user.getUsername());
        System.out.println("Email: " + user.getEmail());
        System.out.println("First Name: " + user.getFirstName());
        System.out.println("Last Name: " + user.getLastName());

        // Validate username
        if (user.getUsername() == null || user.getUsername().trim().length() < 3) {
            model.addAttribute("error", "Username must be at least 3 characters long");
            return "student/register";
        }

        // Validate email
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            model.addAttribute("error", "Please provide a valid email address");
            return "student/register";
        }

        // Validate password
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            model.addAttribute("error", "Password must be at least 6 characters long");
            return "student/register";
        }

        // Validate first name
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            model.addAttribute("error", "First name is required");
            return "student/register";
        }

        // Validate last name
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            model.addAttribute("error", "Last name is required");
            return "student/register";
        }

        // Check if username already exists
        if (userService.usernameExists(user.getUsername())) {
            model.addAttribute("error", "Username already exists");
            return "student/register";
        }

        // Check if email already exists
        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("error", "Email already exists");
            return "student/register";
        }

        try {
            // Register the student
            String password = user.getPassword();
            User registeredStudent = userService.registerStudent(user, password);

            System.out.println("Student registered successfully: " + registeredStudent.getUsername());
            redirectAttributes.addFlashAttribute("message",
                    "Registration successful! Please login with your credentials.");
            return "redirect:/login";
        } catch (Exception e) {
            System.out.println("Error during registration: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error during registration: " + e.getMessage());
            return "student/register";
        }
    }

    /**
     * Dashboard - Role-based redirect after login
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // Get username
        String username = authentication.getName();
        
        // Get user role and redirect accordingly
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("");

        System.out.println("Dashboard redirect for role: " + role + ", username: " + username);

        switch (role) {
            case "ROLE_STUDENT":
                return "redirect:/student/dashboard";
            case "ROLE_STAFF":
                // Staff Portal Dashboard (NEW)
                return "redirect:/staff/dashboard";
            case "ROLE_ADMIN":
                // Differentiate between Admin Portal and User Management
                if ("admin".equals(username)) {
                    // Admin Portal (new integrated portal)
                    return "redirect:/admin/dashboard";
                } else {
                    // User Management (original admin portal)
                    return "redirect:/users";
                }
            default:
                return "redirect:/login";
        }
    }

    /**
     * Home page (root)
     */
    @GetMapping("/home")
    public String home() {
        return "redirect:/";
    }
}
