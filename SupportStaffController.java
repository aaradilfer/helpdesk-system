package com.helpdesk.controller;

import com.helpdesk.service.ResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/support-staff")
public class SupportStaffController {

    @Autowired
    private ResponseService responseService;

    @Value("${support.staff.username:}")
    private String staffUsername;

    @Value("${support.staff.password:}")
    private String staffPassword;

    @GetMapping("/login")
    public String supportStaffLogin() {
        return "support-staff/login";
    }

    @PostMapping("/login")
    public String supportStaffLoginPost(@RequestParam String username,
                                        @RequestParam String password,
                                        jakarta.servlet.http.HttpServletRequest request,
                                        jakarta.servlet.http.HttpServletResponse response) {
        if (username != null && password != null
                && username.equals(staffUsername)
                && password.equals(staffPassword)) {
            // Establish Spring Security authentication for support staff
            java.util.List<SimpleGrantedAuthority> authorities = java.util.List.of(new SimpleGrantedAuthority("ROLE_STAFF"));
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
            
            // Create and set the security context
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);
            
            // Save the security context to the session using SecurityContextRepository
            SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
            securityContextRepository.saveContext(context, request, response);
            
            return "redirect:/support-staff/dashboard-staff";
        }
        return "redirect:/support-staff/login?error";
    }

    @GetMapping("/dashboard-staff")
    public String dashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            model.addAttribute("username", username);

            // Get the first authority (role)
            if (!auth.getAuthorities().isEmpty()) {
                String role = auth.getAuthorities().iterator().next().getAuthority();
                model.addAttribute("role", role.replace("ROLE_", ""));
            }

            // Get response count for the user
            int responseCount = responseService.getResponsesByUser(username).size();
            model.addAttribute("responseCount", responseCount);
        }
        return "dashboard-staff/index";
    }

    @GetMapping("/tickets")
    public String tickets(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            model.addAttribute("username", auth.getName());
            if (!auth.getAuthorities().isEmpty()) {
                String role = auth.getAuthorities().iterator().next().getAuthority();
                model.addAttribute("role", role.replace("ROLE_", ""));
            }
        }
        model.addAttribute("message", "Ticket Management module is not implemented yet. This is a placeholder page.");
        return "support-staff/tickets/list";
    }

    @GetMapping("/logout")
    public String logout(jakarta.servlet.http.HttpServletRequest request) {
        // Clear Spring Security context
        SecurityContextHolder.clearContext();
        
        // Invalidate session
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // Redirect to index.html
        return "redirect:/";
    }
}
