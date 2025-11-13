package com.helpdesk.dto;

import java.util.List;
import java.util.Map;

public class DashboardStatsDTO {

    private Long totalTickets;
    private Long openTickets;
    private Long inProgressTickets;
    private Long resolvedTickets;
    private Long closedTickets;
    private Double averageResolutionTimeHours;
    private List<CategoryStatsDTO> ticketsPerCategory;
    private List<MonthlyTrendDTO> monthlyTrend;
    private List<StudentStatsDTO> topStudents;

    // Constructors
    public DashboardStatsDTO() {}

    // Getters and Setters
    public Long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(Long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public Long getOpenTickets() {
        return openTickets;
    }

    public void setOpenTickets(Long openTickets) {
        this.openTickets = openTickets;
    }

    public Long getInProgressTickets() {
        return inProgressTickets;
    }

    public void setInProgressTickets(Long inProgressTickets) {
        this.inProgressTickets = inProgressTickets;
    }

    public Long getResolvedTickets() {
        return resolvedTickets;
    }

    public void setResolvedTickets(Long resolvedTickets) {
        this.resolvedTickets = resolvedTickets;
    }

    public Long getClosedTickets() {
        return closedTickets;
    }

    public void setClosedTickets(Long closedTickets) {
        this.closedTickets = closedTickets;
    }

    public Double getAverageResolutionTimeHours() {
        return averageResolutionTimeHours;
    }

    public void setAverageResolutionTimeHours(Double averageResolutionTimeHours) {
        this.averageResolutionTimeHours = averageResolutionTimeHours;
    }

    public List<CategoryStatsDTO> getTicketsPerCategory() {
        return ticketsPerCategory;
    }

    public void setTicketsPerCategory(List<CategoryStatsDTO> ticketsPerCategory) {
        this.ticketsPerCategory = ticketsPerCategory;
    }

    public List<MonthlyTrendDTO> getMonthlyTrend() {
        return monthlyTrend;
    }

    public void setMonthlyTrend(List<MonthlyTrendDTO> monthlyTrend) {
        this.monthlyTrend = monthlyTrend;
    }

    public List<StudentStatsDTO> getTopStudents() {
        return topStudents;
    }

    public void setTopStudents(List<StudentStatsDTO> topStudents) {
        this.topStudents = topStudents;
    }

    // Inner classes for nested data
    public static class CategoryStatsDTO {
        private String categoryName;
        private Long ticketCount;

        public CategoryStatsDTO(String categoryName, Long ticketCount) {
            this.categoryName = categoryName;
            this.ticketCount = ticketCount;
        }

        // Getters and Setters
        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public Long getTicketCount() {
            return ticketCount;
        }

        public void setTicketCount(Long ticketCount) {
            this.ticketCount = ticketCount;
        }
    }

    public static class MonthlyTrendDTO {
        private Integer year;
        private Integer month;
        private Long ticketCount;

        public MonthlyTrendDTO(Integer year, Integer month, Long ticketCount) {
            this.year = year;
            this.month = month;
            this.ticketCount = ticketCount;
        }

        // Getters and Setters
        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

        public Integer getMonth() {
            return month;
        }

        public void setMonth(Integer month) {
            this.month = month;
        }

        public Long getTicketCount() {
            return ticketCount;
        }

        public void setTicketCount(Long ticketCount) {
            this.ticketCount = ticketCount;
        }
    }

    public static class StudentStatsDTO {
        private String studentName;
        private String studentId;
        private Long ticketCount;

        public StudentStatsDTO(String studentName, String studentId, Long ticketCount) {
            this.studentName = studentName;
            this.studentId = studentId;
            this.ticketCount = ticketCount;
        }

        // Getters and Setters
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

        public Long getTicketCount() {
            return ticketCount;
        }

        public void setTicketCount(Long ticketCount) {
            this.ticketCount = ticketCount;
        }
    }
}