package com.helpdesk.controller;

import com.helpdesk.entity.User;
import com.helpdesk.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/users")
public class UserManagementController {

    private static final Logger logger = LoggerFactory.getLogger(UserManagementController.class);

    private final UserService userService;

    @Autowired
    public UserManagementController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listUsers(
            @RequestParam(required = false) String search,
            Model model) {

        logger.debug("Listing users with search: {}", search);
        try {
            List<User> users = userService.searchUsers(search);
            logger.debug("Found {} users", users.size());
            model.addAttribute("users", users);
            model.addAttribute("search", search);
            return "users/list";
        } catch (Exception e) {
            logger.error("Error listing users: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Error loading users: " + e.getMessage());
            return "users/list";
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        logger.debug("Showing create user form");
        model.addAttribute("user", new User());
        model.addAttribute("roles", User.UserRole.values());
        model.addAttribute("statuses", User.UserStatus.values());
        model.addAttribute("mode", "create");
        return "users/form";
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PostMapping
    @Transactional
    public String createUser(@Valid @ModelAttribute User user,
                             BindingResult result,
                             @RequestParam String password,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        logger.info("=== CREATE USER REQUEST ===");
        logger.info("Attempting to create user: {}", user.getEmail());
        logger.info("Full Name: {}", user.getFullName());
        logger.info("Role: {}", user.getRole());
        logger.info("Status: {}", user.getStatus());

        if (result.hasErrors()) {
            logger.warn("Validation errors: {}", result.getAllErrors());
            model.addAttribute("roles", User.UserRole.values());
            model.addAttribute("statuses", User.UserStatus.values());
            model.addAttribute("mode", "create");
            return "users/form";
        }

        // Password validation
        if (password == null || password.trim().isEmpty()) {
            logger.warn("Password is empty");
            model.addAttribute("errorMessage", "Password is required");
            model.addAttribute("roles", User.UserRole.values());
            model.addAttribute("statuses", User.UserStatus.values());
            model.addAttribute("mode", "create");
            return "users/form";
        }

        if (password.length() < 6) {
            logger.warn("Password too short");
            model.addAttribute("errorMessage", "Password must be at least 6 characters long");
            model.addAttribute("roles", User.UserRole.values());
            model.addAttribute("statuses", User.UserStatus.values());
            model.addAttribute("mode", "create");
            return "users/form";
        }

        try {
            User savedUser = userService.createUser(user, password);
            logger.info("✓ User created successfully with ID: {}", savedUser.getId());

            redirectAttributes.addFlashAttribute("successMessage",
                    "User created successfully: " + savedUser.getFullName());
            return "redirect:/users/" + savedUser.getId();

        } catch (Exception e) {
            logger.error("✗ Error creating user {}: {}", user.getEmail(), e.getMessage(), e);

            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("already exists")) {
                model.addAttribute("errorMessage", "A user with this email already exists");
            } else {
                model.addAttribute("errorMessage", "Error creating user: " + errorMessage);
            }

            model.addAttribute("roles", User.UserRole.values());
            model.addAttribute("statuses", User.UserStatus.values());
            model.addAttribute("mode", "create");
            return "users/form";
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        logger.debug("Showing edit form for user ID: {}", id);
        try {
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                logger.debug("Loading user for editing: {}", user.getEmail());
                model.addAttribute("user", user);
                model.addAttribute("roles", User.UserRole.values());
                model.addAttribute("statuses", User.UserStatus.values());
                model.addAttribute("mode", "edit");
                return "users/form";
            } else {
                logger.warn("User not found for editing: {}", id);
                return "redirect:/users";
            }
        } catch (Exception e) {
            logger.error("Error loading user for editing {}: {}", id, e.getMessage(), e);
            return "redirect:/users";
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PostMapping("/{id}")
    public String updateUser(@PathVariable Long id,
                             @Valid @ModelAttribute User user,
                             BindingResult result,
                             @RequestParam(required = false) String password,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        logger.info("Attempting to update user ID: {}", id);

        if (result.hasErrors()) {
            logger.warn("Validation errors while updating user {}: {}", id, result.getAllErrors());
            model.addAttribute("roles", User.UserRole.values());
            model.addAttribute("statuses", User.UserStatus.values());
            model.addAttribute("mode", "edit");
            return "users/form";
        }

        if (password != null && !password.trim().isEmpty() && password.length() < 6) {
            logger.warn("New password too short for user: {}", id);
            model.addAttribute("errorMessage", "Password must be at least 6 characters long");
            model.addAttribute("roles", User.UserRole.values());
            model.addAttribute("statuses", User.UserStatus.values());
            model.addAttribute("mode", "edit");
            return "users/form";
        }

        try {
            User updatedUser = userService.updateUserWithPassword(id, user, password);
            logger.info("User updated successfully: {}", updatedUser.getEmail());

            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully");
            return "redirect:/users/" + updatedUser.getId();

        } catch (Exception e) {
            logger.error("Error updating user {}: {}", id, e.getMessage(), e);

            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("already exists")) {
                model.addAttribute("errorMessage", "A user with this email already exists");
            } else {
                model.addAttribute("errorMessage", "Error updating user: " + errorMessage);
            }

            model.addAttribute("roles", User.UserRole.values());
            model.addAttribute("statuses", User.UserStatus.values());
            model.addAttribute("mode", "edit");
            return "users/form";
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("Attempting to delete user ID: {}", id);
        try {
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                userService.deleteUser(id);
                logger.info("User deleted successfully: {} ({})", user.getEmail(), id);
                redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully");
            } else {
                logger.warn("User not found for deletion: {}", id);
                redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            }
        } catch (Exception e) {
            logger.error("Error deleting user {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/users";
    }

    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    @PostMapping("/{id}/toggle")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        logger.info("Attempting to toggle status for user ID: {}", id);
        try {
            User user = userService.toggleUserStatus(id);
            String statusMessage = user.getStatus() == User.UserStatus.ACTIVE ? "activated" : "deactivated";
            logger.info("User status toggled successfully: {} is now {}", user.getEmail(), user.getStatus());

            redirectAttributes.addFlashAttribute("successMessage",
                    "User " + statusMessage + " successfully");
        } catch (Exception e) {
            logger.error("Error toggling user status {}: {}", id, e.getMessage(), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating user status: " + e.getMessage());
        }
        return "redirect:/users";
    }

    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model) {
        logger.debug("Viewing user details for ID: {}", id);
        try {
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                logger.debug("Displaying user: {}", user.getEmail());
                model.addAttribute("user", user);
                return "users/view";
            } else {
                logger.warn("User not found for viewing: {}", id);
                return "redirect:/users";
            }
        } catch (Exception e) {
            logger.error("Error loading user for viewing {}: {}", id, e.getMessage(), e);
            return "redirect:/users";
        }
    }
}