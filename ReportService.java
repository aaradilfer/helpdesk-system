package com.helpdesk.service;

import com.helpdesk.entity.SavedReport;
import com.helpdesk.entity.Ticket;
import com.helpdesk.repository.SavedReportRepository;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReportService {

    @Autowired
    private SavedReportRepository savedReportRepository;

    @Autowired
    private TicketService ticketService;

    // Saved Report CRUD Operations
    public SavedReport createSavedReport(SavedReport savedReport) {
        if (savedReportRepository.existsByNameAndCreatedBy(savedReport.getName(), savedReport.getCreatedBy())) {
            throw new RuntimeException("Report with name '" + savedReport.getName() + "' already exists for this user");
        }
        return savedReportRepository.save(savedReport);
    }

    public Optional<SavedReport> getSavedReportById(Long id) {
        return savedReportRepository.findById(id);
    }

    public List<SavedReport> getAllSavedReports() {
        return savedReportRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<SavedReport> getSavedReportsByUser(String createdBy) {
        return savedReportRepository.findByCreatedByOrderByCreatedAtDesc(createdBy);
    }

    public SavedReport updateSavedReport(SavedReport savedReport) {
        return savedReportRepository.save(savedReport);
    }

    public void deleteSavedReport(Long id) {
        savedReportRepository.deleteById(id);
    }

    // Report Generation Methods
    public String generateCSVReport(List<Ticket> tickets) throws IOException {
        StringWriter stringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(stringWriter);

        // Write header
        String[] header = {
                "ID", "Title", "Description", "Student Name", "Student ID", "Student Email",
                "Student Phone", "Priority", "Status", "Category", "Assigned Staff",
                "Created At", "Updated At", "Resolved At", "Resolution Notes"
        };
        csvWriter.writeNext(header);

        // Write data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Ticket ticket : tickets) {
            String[] row = {
                    ticket.getId().toString(),
                    ticket.getTitle(),
                    ticket.getDescription(),
                    ticket.getStudentName(),
                    ticket.getStudentId(),
                    ticket.getStudentEmail(),
                    ticket.getStudentPhone() != null ? ticket.getStudentPhone() : "",
                    ticket.getPriority().toString(),
                    ticket.getStatus().toString(),
                    ticket.getCategory().getName(),
                    ticket.getAssignedStaff() != null ? ticket.getAssignedStaff().getName() : "",
                    ticket.getCreatedAt().format(formatter),
                    ticket.getUpdatedAt() != null ? ticket.getUpdatedAt().format(formatter) : "",
                    ticket.getResolvedAt() != null ? ticket.getResolvedAt().format(formatter) : "",
                    ticket.getResolutionNotes() != null ? ticket.getResolutionNotes() : ""
            };
            csvWriter.writeNext(row);
        }

        csvWriter.close();
        return stringWriter.toString();
    }

    public byte[] generateExcelReport(List<Ticket> tickets) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Tickets Report");

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "ID", "Title", "Description", "Student Name", "Student ID", "Student Email",
                "Student Phone", "Priority", "Status", "Category", "Assigned Staff",
                "Created At", "Updated At", "Resolved At", "Resolution Notes"
        };

        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Create data rows
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        int rowNum = 1;
        for (Ticket ticket : tickets) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(ticket.getId());
            row.createCell(1).setCellValue(ticket.getTitle());
            row.createCell(2).setCellValue(ticket.getDescription());
            row.createCell(3).setCellValue(ticket.getStudentName());
            row.createCell(4).setCellValue(ticket.getStudentId());
            row.createCell(5).setCellValue(ticket.getStudentEmail());
            row.createCell(6).setCellValue(ticket.getStudentPhone() != null ? ticket.getStudentPhone() : "");
            row.createCell(7).setCellValue(ticket.getPriority().toString());
            row.createCell(8).setCellValue(ticket.getStatus().toString());
            row.createCell(9).setCellValue(ticket.getCategory().getName());
            row.createCell(10)
                    .setCellValue(ticket.getAssignedStaff() != null ? ticket.getAssignedStaff().getName() : "");
            row.createCell(11).setCellValue(ticket.getCreatedAt().format(formatter));
            row.createCell(12)
                    .setCellValue(ticket.getUpdatedAt() != null ? ticket.getUpdatedAt().format(formatter) : "");
            row.createCell(13)
                    .setCellValue(ticket.getResolvedAt() != null ? ticket.getResolvedAt().format(formatter) : "");
            row.createCell(14).setCellValue(ticket.getResolutionNotes() != null ? ticket.getResolutionNotes() : "");
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    public byte[] generatePDFReport(List<Ticket> tickets) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Add title
        document.add(new Paragraph("Tickets Report")
                .setFontSize(18));

        document.add(new Paragraph("Generated on: " +
                java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .setFontSize(10));

        document.add(new Paragraph("\n"));

        // Create simple table with 8 columns
        Table table = new Table(8);

        // Add headers
        String[] headers = { "ID", "Title", "Student", "Category", "Priority", "Status", "Staff", "Created" };
        for (String header : headers) {
            table.addHeaderCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(header)));
        }

        // Add data rows
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (Ticket ticket : tickets) {
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(ticket.getId().toString())));
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(ticket.getTitle())));
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(ticket.getStudentName() + " (" + ticket.getStudentId() + ")")));
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(ticket.getCategory().getName())));
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(ticket.getPriority().toString())));
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(ticket.getStatus().toString())));
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(
                    ticket.getAssignedStaff() != null ? ticket.getAssignedStaff().getName() : "Unassigned")));
            table.addCell(
                    new com.itextpdf.layout.element.Cell().add(new Paragraph(ticket.getCreatedAt().format(formatter))));
        }

        document.add(table);
        document.close();

        return outputStream.toByteArray();
    }

    // Report aggregation summary
    public ReportSummaryDTO generateReportSummary(LocalDate startDate, LocalDate endDate,
                                                  List<Long> categoryIds, List<Long> staffIds,
                                                  List<Ticket.Status> statuses, String studentName,
                                                  String studentId) {
        Long totalCount = ticketService.countTicketsWithFilters(startDate, endDate, categoryIds,
                staffIds, statuses, studentName, studentId);
        Double avgResolutionTime = ticketService.getAverageResolutionTimeWithFilters(startDate, endDate,
                categoryIds, staffIds,
                statuses, studentName, studentId);

        return new ReportSummaryDTO(totalCount, avgResolutionTime);
    }

    // DTO for report summary
    public static class ReportSummaryDTO {
        private Long totalTickets;
        private Double averageResolutionTimeHours;

        public ReportSummaryDTO(Long totalTickets, Double averageResolutionTimeHours) {
            this.totalTickets = totalTickets;
            this.averageResolutionTimeHours = averageResolutionTimeHours;
        }

        // Getters and Setters
        public Long getTotalTickets() {
            return totalTickets;
        }

        public void setTotalTickets(Long totalTickets) {
            this.totalTickets = totalTickets;
        }

        public Double getAverageResolutionTimeHours() {
            return averageResolutionTimeHours;
        }

        public void setAverageResolutionTimeHours(Double averageResolutionTimeHours) {
            this.averageResolutionTimeHours = averageResolutionTimeHours;
        }
    }
}