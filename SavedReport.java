package com.helpdesk.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "saved_reports")
public class SavedReport extends BaseEntity {

    @NotBlank(message = "Report name is required")
    @Size(max = 100, message = "Report name must not exceed 100 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ElementCollection
    @CollectionTable(name = "saved_report_categories", joinColumns = @JoinColumn(name = "saved_report_id"))
    @Column(name = "category_id")
    private List<Long> categoryIds;

    @ElementCollection
    @CollectionTable(name = "saved_report_staff", joinColumns = @JoinColumn(name = "saved_report_id"))
    @Column(name = "staff_id")
    private List<Long> staffIds;

    @ElementCollection
    @CollectionTable(name = "saved_report_statuses", joinColumns = @JoinColumn(name = "saved_report_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private List<Ticket.Status> statuses;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "student_id")
    private String studentId;

    @Column(name = "created_by")
    private String createdBy;

    // Constructors
    public SavedReport() {}

    public SavedReport(String name, String description, String createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public List<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public List<Long> getStaffIds() {
        return staffIds;
    }

    public void setStaffIds(List<Long> staffIds) {
        this.staffIds = staffIds;
    }

    public List<Ticket.Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<Ticket.Status> statuses) {
        this.statuses = statuses;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
