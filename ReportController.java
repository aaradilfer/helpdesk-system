package com.helpdesk.controller;

import com.helpdesk.entity.SavedReport;
import com.helpdesk.entity.Ticket;
import com.helpdesk.service.CategoryService;
import com.helpdesk.service.ReportService;
import com.helpdesk.service.StaffService;
import com.helpdesk.service.TicketService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StaffService staffService;

    // Show reports page
    @GetMapping
    public String showReportsPage(HttpSession session, Model model) {
        // Check if business admin is logged in via session
        Boolean businessAdminLoggedIn = (Boolean) session.getAttribute("businessAdminLoggedIn");
        
        if (businessAdminLoggedIn == null || !businessAdminLoggedIn) {
            return "redirect:/business-admin/login?error=access_denied";
        }
        
        // Add all required model attributes for the form
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("staff", staffService.getActiveStaff());
        model.addAttribute("allStatuses", Ticket.Status.values());
        model.addAttribute("savedReports", reportService.getAllSavedReports());
        return "reports/index";
    }

    // Simple test endpoint
    @GetMapping("/test")
    public String testEndpoint() {
        System.out.println("Test endpoint called successfully!");
        return "reports/index";
    }

    // Generate report preview
    @PostMapping("/preview")
    public String generateReportPreview(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> staffIds,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String studentId,
            HttpSession session,
            Model model) {
        
        System.out.println("ReportController.generateReportPreview() called successfully!");
        
        // Check if business admin is logged in via session
        Boolean businessAdminLoggedIn = (Boolean) session.getAttribute("businessAdminLoggedIn");
        if (businessAdminLoggedIn == null || !businessAdminLoggedIn) {
            return "redirect:/business-admin/login?error=access_denied";
        }

        // Convert string statuses to enum
        List<Ticket.Status> statusEnums = null;
        if (statuses != null && !statuses.isEmpty()) {
            statusEnums = statuses.stream()
                    .map(Ticket.Status::valueOf)
                    .collect(Collectors.toList());
        }

        // Convert string dates to LocalDate if provided
        LocalDate startDateParsed = null;
        LocalDate endDateParsed = null;
        if (startDate != null && !startDate.trim().isEmpty()) {
            try {
                startDateParsed = LocalDate.parse(startDate);
            } catch (Exception e) {
                System.out.println("Error parsing start date: " + e.getMessage());
            }
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            try {
                endDateParsed = LocalDate.parse(endDate);
            } catch (Exception e) {
                System.out.println("Error parsing end date: " + e.getMessage());
            }
        }

        // Get filtered tickets
        List<Ticket> tickets = ticketService.getTicketsWithFilters(
                startDateParsed, endDateParsed, categoryIds, staffIds, statusEnums, studentName, studentId);

        // Generate summary
        ReportService.ReportSummaryDTO summary = reportService.generateReportSummary(
                startDateParsed, endDateParsed, categoryIds, staffIds, statusEnums, studentName, studentId);

        // Add results to model
        model.addAttribute("tickets", tickets);
        model.addAttribute("summary", summary);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("categoryIds", categoryIds);
        model.addAttribute("staffIds", staffIds);
        model.addAttribute("statuses", statuses);
        model.addAttribute("studentName", studentName);
        model.addAttribute("studentId", studentId);

        // Add filter options for the form
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("staff", staffService.getActiveStaff());
        model.addAttribute("allStatuses", Ticket.Status.values());
        model.addAttribute("savedReports", reportService.getAllSavedReports());

        return "reports/index";
    }

    // Export to CSV
    @PostMapping("/export/csv")
    public void exportToCSV(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> staffIds,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String studentId,
            HttpSession session,
            HttpServletResponse response) throws IOException {
        
        // Check if business admin is logged in via session
        Boolean businessAdminLoggedIn = (Boolean) session.getAttribute("businessAdminLoggedIn");
        
        if (businessAdminLoggedIn == null || !businessAdminLoggedIn) {
            response.sendRedirect("/business-admin/login?error=access_denied");
            return;
        }

        // Convert string statuses to enum
        List<Ticket.Status> statusEnums = null;
        if (statuses != null && !statuses.isEmpty()) {
            statusEnums = statuses.stream()
                    .map(Ticket.Status::valueOf)
                    .collect(Collectors.toList());
        }

        List<Ticket> tickets = ticketService.getTicketsWithFilters(
                startDate, endDate, categoryIds, staffIds, statusEnums, studentName, studentId);

        String csvContent = reportService.generateCSVReport(tickets);

        String filename = "tickets_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                + ".csv";

        response.setContentType("text/csv");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        response.getWriter().write(csvContent);
    }

    // Export to Excel
    @PostMapping("/export/excel")
    public void exportToExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> staffIds,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String studentId,
            HttpSession session,
            HttpServletResponse response) throws IOException {
        
        // Check if business admin is logged in via session
        Boolean businessAdminLoggedIn = (Boolean) session.getAttribute("businessAdminLoggedIn");
        
        if (businessAdminLoggedIn == null || !businessAdminLoggedIn) {
            response.sendRedirect("/business-admin/login?error=access_denied");
            return;
        }

        // Convert string statuses to enum
        List<Ticket.Status> statusEnums = null;
        if (statuses != null && !statuses.isEmpty()) {
            statusEnums = statuses.stream()
                    .map(Ticket.Status::valueOf)
                    .collect(Collectors.toList());
        }

        List<Ticket> tickets = ticketService.getTicketsWithFilters(
                startDate, endDate, categoryIds, staffIds, statusEnums, studentName, studentId);

        byte[] excelContent = reportService.generateExcelReport(tickets);

        String filename = "tickets_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                + ".xlsx";

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        response.getOutputStream().write(excelContent);
    }

    // Export to PDF
    @PostMapping("/export/pdf")
    public void exportToPDF(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> staffIds,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String studentId,
            HttpSession session,
            HttpServletResponse response) throws IOException {
        
        // Check if business admin is logged in via session
        Boolean businessAdminLoggedIn = (Boolean) session.getAttribute("businessAdminLoggedIn");
        
        if (businessAdminLoggedIn == null || !businessAdminLoggedIn) {
            response.sendRedirect("/business-admin/login?error=access_denied");
            return;
        }

        // Convert string statuses to enum
        List<Ticket.Status> statusEnums = null;
        if (statuses != null && !statuses.isEmpty()) {
            statusEnums = statuses.stream()
                    .map(Ticket.Status::valueOf)
                    .collect(Collectors.toList());
        }

        List<Ticket> tickets = ticketService.getTicketsWithFilters(
                startDate, endDate, categoryIds, staffIds, statusEnums, studentName, studentId);

        byte[] pdfContent = reportService.generatePDFReport(tickets);

        String filename = "tickets_report_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                + ".pdf";

        response.setContentType("application/pdf");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        response.getOutputStream().write(pdfContent);
    }

    // Save report configuration
    @PostMapping("/save")
    public String saveReportConfiguration(
            @RequestParam String reportName,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> staffIds,
            @RequestParam(required = false) List<String> statuses,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String studentId,
            RedirectAttributes redirectAttributes) {

        try {
            SavedReport savedReport = new SavedReport();
            savedReport.setName(reportName);
            savedReport.setDescription(description);
            savedReport.setStartDate(startDate);
            savedReport.setEndDate(endDate);
            savedReport.setCategoryIds(categoryIds);
            savedReport.setStaffIds(staffIds);

            // Convert string statuses to enum
            if (statuses != null && !statuses.isEmpty()) {
                List<Ticket.Status> statusEnums = statuses.stream()
                        .map(Ticket.Status::valueOf)
                        .collect(Collectors.toList());
                savedReport.setStatuses(statusEnums);
            }

            savedReport.setStudentName(studentName);
            savedReport.setStudentId(studentId);
            savedReport.setCreatedBy("admin"); // In a real app, get from authentication

            reportService.createSavedReport(savedReport);
            redirectAttributes.addFlashAttribute("successMessage", "Report configuration saved successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving report: " + e.getMessage());
        }

        return "redirect:/admin/reports";
    }

    // Load saved report configuration
    @GetMapping("/load/{id}")
    public String loadSavedReport(@PathVariable Long id, Model model) {
        Optional<SavedReport> savedReportOpt = reportService.getSavedReportById(id);
        if (savedReportOpt.isPresent()) {
            SavedReport savedReport = savedReportOpt.get();

            // Convert enum statuses to strings
            List<String> statusStrings = null;
            if (savedReport.getStatuses() != null) {
                statusStrings = savedReport.getStatuses().stream()
                        .map(Enum::name)
                        .collect(Collectors.toList());
            }

            // Generate preview with saved configuration
            List<Ticket> tickets = ticketService.getTicketsWithFilters(
                    savedReport.getStartDate(), savedReport.getEndDate(),
                    savedReport.getCategoryIds(), savedReport.getStaffIds(),
                    savedReport.getStatuses(), savedReport.getStudentName(),
                    savedReport.getStudentId());

            ReportService.ReportSummaryDTO summary = reportService.generateReportSummary(
                    savedReport.getStartDate(), savedReport.getEndDate(),
                    savedReport.getCategoryIds(), savedReport.getStaffIds(),
                    savedReport.getStatuses(), savedReport.getStudentName(),
                    savedReport.getStudentId());

            model.addAttribute("tickets", tickets);
            model.addAttribute("summary", summary);
            model.addAttribute("startDate", savedReport.getStartDate());
            model.addAttribute("endDate", savedReport.getEndDate());
            model.addAttribute("categoryIds", savedReport.getCategoryIds());
            model.addAttribute("staffIds", savedReport.getStaffIds());
            model.addAttribute("statuses", statusStrings);
            model.addAttribute("studentName", savedReport.getStudentName());
            model.addAttribute("studentId", savedReport.getStudentId());
            model.addAttribute("loadedReportName", savedReport.getName());
        }

        // Add filter options for the form
        model.addAttribute("categories", categoryService.getActiveCategories());
        model.addAttribute("staff", staffService.getActiveStaff());
        model.addAttribute("allStatuses", Ticket.Status.values());
        model.addAttribute("savedReports", reportService.getAllSavedReports());

        return "reports/index";
    }

    // Delete saved report
    @PostMapping("/delete/{id}")
    public String deleteSavedReport(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reportService.deleteSavedReport(id);
            redirectAttributes.addFlashAttribute("successMessage", "Saved report deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting report: " + e.getMessage());
        }
        return "redirect:/admin/reports";
    }
}