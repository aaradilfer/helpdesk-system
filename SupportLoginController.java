package com.helpdesk.controller;

import com.helpdesk.entity.SupportTeamUser;
import com.helpdesk.service.SupportTeamUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class SupportLoginController {

    @Autowired
    private SupportTeamUserService supportTeamUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/support/login")
    public String supportLoginPage(@RequestParam(required = false) String error,
                                    @RequestParam(required = false) String logout,
                                    Model model) {
        if (error != null) {
            model.addAttribute("error", true);
        }
        if (logout != null) {
            model.addAttribute("logout", true);
        }
        return "support/login";
    }

    @PostMapping("/support/login")
    public String supportLoginPost(@RequestParam String username,
                                    @RequestParam String password,
                                    HttpSession session,
                                    Model model) {
        // Authenticate user
        Optional<SupportTeamUser> userOpt = supportTeamUserService.getUserByUsername(username);
        
        if (userOpt.isPresent()) {
            SupportTeamUser user = userOpt.get();
            
            // Check if user is active and password matches
            if (user.getIsActive() && passwordEncoder.matches(password, user.getPasswordHash())) {
                // Set session attributes
                session.setAttribute("lecturerUser", user.getUsername());
                session.setAttribute("lecturerUserId", user.getId());
                session.setAttribute("lecturerFullName", user.getFullName());
                
                // Redirect to dashboard
                return "redirect:/support/dashboard";
            }
        }
        
        // Login failed
        model.addAttribute("error", true);
        return "support/login";
    }

    @GetMapping("/support/logout")
    public String supportLogout(HttpSession session) {
        // Invalidate session
        session.invalidate();
        return "redirect:/support/login?logout=true";
    }
}

