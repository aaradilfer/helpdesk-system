package com.helpdesk.controller;

import com.helpdesk.dto.SupportStatsDTO;
import com.helpdesk.service.SupportArticleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SupportDashboardController {

    @Autowired
    private SupportArticleService supportArticleService;

    @GetMapping("/support/dashboard")
    public String dashboard(HttpSession session, Model model) {
        // Check if user is logged in (session-based authentication)
        String lecturerUser = (String) session.getAttribute("lecturerUser");
        if (lecturerUser == null) {
            return "redirect:/support/login";
        }

        SupportStatsDTO stats = getSupportStats();
        model.addAttribute("stats", stats);
        model.addAttribute("username", lecturerUser);
        model.addAttribute("fullName", session.getAttribute("lecturerFullName"));
        
        return "support/dashboard";
    }

    private SupportStatsDTO getSupportStats() {
        SupportStatsDTO stats = new SupportStatsDTO();

        try {
            stats.setPublishedArticles(supportArticleService.getPublishedArticlesCount());
            stats.setDraftArticles(supportArticleService.getDraftArticlesCount());
            stats.setArchivedArticles(supportArticleService.getArchivedArticlesCount());
            stats.setTotalViews(supportArticleService.getTotalViews());

            Long total = (stats.getPublishedArticles() != null ? stats.getPublishedArticles() : 0) +
                    (stats.getDraftArticles() != null ? stats.getDraftArticles() : 0) +
                    (stats.getArchivedArticles() != null ? stats.getArchivedArticles() : 0);
            stats.setTotalArticles(total);

        } catch (Exception e) {
            // Set default values if there's an error
            stats.setTotalArticles(0L);
            stats.setPublishedArticles(0L);
            stats.setDraftArticles(0L);
            stats.setArchivedArticles(0L);
            stats.setTotalViews(0L);
        }

        return stats;
    }

    @GetMapping("/support")
    public String redirectToDashboard() {
        return "redirect:/support/dashboard";
    }
}

