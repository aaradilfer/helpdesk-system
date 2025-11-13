package com.helpdesk.controller;

import com.helpdesk.entity.User;
import com.helpdesk.service.PaymentTeamUserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/payment")
public class PaymentLoginController {

    @Autowired
    private PaymentTeamUserService paymentTeamUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String showLoginPage() {
        return "payment/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                       @RequestParam String password,
                       HttpSession session,
                       Model model) {
        // Authenticate user
        Optional<User> userOpt = paymentTeamUserService.getUserByUsername(username);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Check password
            if (passwordEncoder.matches(password, user.getPasswordHash()) && user.getEnabled()) {
                // Set session attribute
                session.setAttribute("paymentUser", username);
                session.setAttribute("paymentUserId", user.getId());
                return "redirect:/payment/dashboard";
            }
        }
        
        // Login failed
        return "redirect:/payment/login?error";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute("paymentUser");
        session.removeAttribute("paymentUserId");
        session.invalidate();
        return "redirect:/payment/login?logout";
    }
}

