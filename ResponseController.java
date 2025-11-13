package com.helpdesk.controller;

import com.helpdesk.entity.ResponseTemplate;
import com.helpdesk.service.ResponseService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/responses")
public class ResponseController {

    private final ResponseService responseService;

    public ResponseController(ResponseService responseService) {
        this.responseService = responseService;
    }

    @GetMapping
    public String listResponses(Authentication authentication, Model model) {
        // Get authentication from SecurityContextHolder if parameter is null
        if (authentication == null) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/support-staff/login";
        }
        
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        List<ResponseTemplate> responses;
        boolean isAdmin = role.equals("ROLE_ADMIN");

        if (isAdmin) {
            responses = responseService.getAllResponses();
        } else {
            responses = responseService.getUserAndAdminResponses(username);
        }

        model.addAttribute("responses", responses);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("username", username); // Add username to model
        return "response/list";
    }

    @GetMapping("/my")
    public String myResponses(Authentication authentication, Model model) {
        // Get authentication from SecurityContextHolder if parameter is null
        if (authentication == null) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/support-staff/login";
        }
        
        String username = authentication.getName();
        List<ResponseTemplate> responses = responseService.getResponsesByUser(username);
        model.addAttribute("responses", responses);
        model.addAttribute("username", username); // Add username to model
        return "response/my-list";
    }

    @GetMapping("/create")
    public String createForm(Authentication authentication, Model model) {
        // Get authentication from SecurityContextHolder if parameter is null
        if (authentication == null) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/support-staff/login";
        }
        
        model.addAttribute("responseTemplate", new ResponseTemplate());
        model.addAttribute("username", authentication.getName()); // Add username to model
        return "response/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Authentication authentication, Model model) {
        // Get authentication from SecurityContextHolder if parameter is null
        if (authentication == null) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/support-staff/login";
        }
        
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        ResponseTemplate template = responseService.findById(id)
                .orElseThrow(() -> new RuntimeException("Response template not found"));

        // Check permission
        if (!role.equals("ROLE_ADMIN") && !template.getCreatedBy().equals(username)) {
            return "redirect:/responses";
        }

        model.addAttribute("responseTemplate", template);
        model.addAttribute("username", username); // Add username to model
        return "response/form";
    }

    @PostMapping("/save")
    public String saveResponse(@ModelAttribute ResponseTemplate template,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        // Get authentication from SecurityContextHolder if parameter is null
        if (authentication == null) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/support-staff/login";
        }
        
        String username = authentication.getName();

        if (template.getId() == null) {
            template.setCreatedBy(username);
            template.setCreatedAt(LocalDateTime.now());
        } else {
            // For updates, ensure user owns the template or is admin
            ResponseTemplate existing = responseService.findById(template.getId())
                    .orElseThrow(() -> new RuntimeException("Response template not found"));

            String role = authentication.getAuthorities().iterator().next().getAuthority();
            if (!role.equals("ROLE_ADMIN") && !existing.getCreatedBy().equals(username)) {
                redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to edit this template");
                return "redirect:/responses";
            }
        }

        template.setUpdatedAt(LocalDateTime.now());
        responseService.save(template);
        redirectAttributes.addFlashAttribute("successMessage", "Response template saved successfully");
        return "redirect:/responses";
    }

    @PostMapping("/delete/{id}")
    public String deleteResponse(@PathVariable Long id, Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        // Get authentication from SecurityContextHolder if parameter is null
        if (authentication == null) {
            authentication = SecurityContextHolder.getContext().getAuthentication();
        }
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/support-staff/login";
        }
        
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        ResponseTemplate template = responseService.findById(id)
                .orElseThrow(() -> new RuntimeException("Response template not found"));

        // Check permission
        if (!role.equals("ROLE_ADMIN") && !template.getCreatedBy().equals(username)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to delete this template");
            return "redirect:/responses";
        }

        responseService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Response template deleted successfully");
        return "redirect:/responses";
    }
}