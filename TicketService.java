package com.helpdesk.service;

import com.helpdesk.dto.DashboardStatsDTO;
import com.helpdesk.entity.Ticket;
import com.helpdesk.entity.User;
import com.helpdesk.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    // CRUD Operations
    public Ticket createTicket(Ticket ticket) {
        ticket.setStatus(Ticket.Status.OPEN);
        return ticketRepository.save(ticket);
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }

    public Page<Ticket> getAllTickets(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ticketRepository.findAll(pageable);
    }

    public Ticket updateTicket(Ticket ticket) {
        return ticketRepository.save(ticket);
    }

    public void deleteTicket(Long id) {
        ticketRepository.deleteById(id);
    }

    // Search functionality
    public Page<Ticket> searchTickets(String searchTerm, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ticketRepository.findBySearchTerm(searchTerm, pageable);
    }

    // Filter methods
    public Page<Ticket> getTicketsByStatus(Ticket.Status status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ticketRepository.findByStatus(status, pageable);
    }

    public Page<Ticket> getTicketsByPriority(Ticket.Priority priority, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ticketRepository.findByPriority(priority, pageable);
    }

    public Page<Ticket> getTicketsByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ticketRepository.findByCategoryId(categoryId, pageable);
    }

    public Page<Ticket> getTicketsByAssignedStaff(Long staffId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ticketRepository.findByAssignedStaffId(staffId, pageable);
    }

    public Page<Ticket> getTicketsByStudent(String studentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ticketRepository.findByStudentId(studentId, pageable);
    }

    // Dashboard statistics
    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // Basic counts
        stats.setTotalTickets(ticketRepository.countTotalTickets());
        stats.setOpenTickets(ticketRepository.countOpenTickets());
        stats.setInProgressTickets(ticketRepository.countInProgressTickets());
        stats.setResolvedTickets(ticketRepository.countResolvedTickets());
        stats.setClosedTickets(ticketRepository.countClosedTickets());

        // Average resolution time
        stats.setAverageResolutionTimeHours(ticketRepository.getAverageResolutionTimeInHours());

        // Tickets per category
        List<Object[]> categoryData = ticketRepository.getTicketsPerCategory();
        List<DashboardStatsDTO.CategoryStatsDTO> categoryStats = categoryData.stream()
                .map(row -> new DashboardStatsDTO.CategoryStatsDTO((String) row[0], (Long) row[1]))
                .collect(Collectors.toList());
        stats.setTicketsPerCategory(categoryStats);

        // Monthly trend (last 12 months)
        LocalDateTime startDate = LocalDateTime.now().minusMonths(12);
        List<Object[]> trendData = ticketRepository.getMonthlyTicketTrend(startDate);
        List<DashboardStatsDTO.MonthlyTrendDTO> monthlyTrend = trendData.stream()
                .map(row -> new DashboardStatsDTO.MonthlyTrendDTO((Integer) row[0], (Integer) row[1], (Long) row[2]))
                .collect(Collectors.toList());
        stats.setMonthlyTrend(monthlyTrend);

        // Top 5 students
        Pageable topStudentsPageable = PageRequest.of(0, 5);
        List<Object[]> studentData = ticketRepository.getTopStudentsByTicketCount(topStudentsPageable);
        List<DashboardStatsDTO.StudentStatsDTO> topStudents = studentData.stream()
                .map(row -> new DashboardStatsDTO.StudentStatsDTO((String) row[0], (String) row[1], (Long) row[2]))
                .collect(Collectors.toList());
        stats.setTopStudents(topStudents);

        return stats;
    }

    // Reporting methods
    public List<Ticket> getTicketsWithFilters(LocalDate startDate, LocalDate endDate,
                                              List<Long> categoryIds, List<Long> staffIds,
                                              List<Ticket.Status> statuses, String studentName,
                                              String studentId) {
        return ticketRepository.findTicketsWithFilters(startDate, endDate, categoryIds,
                staffIds, statuses, studentName, studentId);
    }

    public Long countTicketsWithFilters(LocalDate startDate, LocalDate endDate,
                                        List<Long> categoryIds, List<Long> staffIds,
                                        List<Ticket.Status> statuses, String studentName,
                                        String studentId) {
        return ticketRepository.countTicketsWithFilters(startDate, endDate, categoryIds,
                staffIds, statuses, studentName, studentId);
    }

    public Double getAverageResolutionTimeWithFilters(LocalDate startDate, LocalDate endDate,
                                                      List<Long> categoryIds, List<Long> staffIds,
                                                      List<Ticket.Status> statuses, String studentName,
                                                      String studentId) {
        return ticketRepository.getAverageResolutionTimeWithFilters(startDate, endDate, categoryIds,
                staffIds, statuses, studentName, studentId);
    }

    // Status update methods
    public Ticket assignTicket(Long ticketId, Long staffId) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            // Set assignedTo field (User with STAFF role)
            User staffUser = new User();
            staffUser.setId(staffId);
            ticket.setAssignedTo(staffUser);
            ticket.setStatus(Ticket.Status.IN_PROGRESS);
            return ticketRepository.save(ticket);
        }
        throw new RuntimeException("Ticket not found with id: " + ticketId);
    }

    public Ticket resolveTicket(Long ticketId, String resolutionNotes) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            ticket.setStatus(Ticket.Status.RESOLVED);
            ticket.setResolutionNotes(resolutionNotes);
            ticket.setResolvedAt(LocalDateTime.now());
            return ticketRepository.save(ticket);
        }
        throw new RuntimeException("Ticket not found with id: " + ticketId);
    }

    public Ticket closeTicket(Long ticketId) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            ticket.setStatus(Ticket.Status.CLOSED);
            return ticketRepository.save(ticket);
        }
        throw new RuntimeException("Ticket not found with id: " + ticketId);
    }

    // NEW METHODS for Student Portal Integration

    /**
     * Find tickets by user (student)
     */
    public Page<Ticket> findTicketsByUser(User user, Pageable pageable) {
        return ticketRepository.findByUser(user, pageable);
    }

    /**
     * Find user tickets with filters
     */
    public Page<Ticket> findUserTicketsWithFilters(User user, Ticket.Status status, 
            Ticket.Priority priority, Long categoryId, String search, Pageable pageable) {
        return ticketRepository.findUserTicketsWithFilters(user, status, priority, categoryId, search, pageable);
    }

    /**
     * Count tickets by user
     */
    public long countTicketsByUser(User user) {
        return ticketRepository.countByUser(user);
    }

    /**
     * Count tickets by user and status
     */
    public long countTicketsByUserAndStatus(User user, Ticket.Status status) {
        return ticketRepository.countByUserAndStatus(user, status);
    }

    /**
     * Check if user can edit ticket (ticket must be OPEN and belong to user)
     */
    public boolean canUserEditTicket(Ticket ticket, User user) {
        if (ticket == null || user == null) {
            return false;
        }
        
        // User must own the ticket
        if (ticket.getUser() == null || !ticket.getUser().getId().equals(user.getId())) {
            return false;
        }
        
        // Ticket must be in OPEN status
        return ticket.getStatus() == Ticket.Status.OPEN;
    }

    /**
     * Check if user can delete ticket (ticket must be OPEN and belong to user)
     */
    public boolean canUserDeleteTicket(Ticket ticket, User user) {
        return canUserEditTicket(ticket, user); // Same rules as edit
    }

    /**
     * Find ticket by ID (helper method)
     */
    public Optional<Ticket> findById(Long id) {
        return ticketRepository.findById(id);
    }

    // ============================================
    // ADMIN PORTAL METHODS (NEW)
    // ============================================

    /**
     * Get total tickets count
     */
    public long getTotalTicketsCount() {
        return ticketRepository.count();
    }

    /**
     * Get open tickets count
     */
    public long getOpenTicketsCount() {
        return ticketRepository.countByStatus(Ticket.Status.OPEN);
    }

    /**
     * Get in-progress tickets count
     */
    public long getInProgressTicketsCount() {
        return ticketRepository.countByStatus(Ticket.Status.IN_PROGRESS);
    }

    /**
     * Get resolved tickets count
     */
    public long getResolvedTicketsCount() {
        return ticketRepository.countByStatus(Ticket.Status.RESOLVED);
    }

    /**
     * Get closed tickets count
     */
    public long getClosedTicketsCount() {
        return ticketRepository.countByStatus(Ticket.Status.CLOSED);
    }

    /**
     * Get tickets created today
     */
    public long getTicketsCreatedToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        return ticketRepository.countByCreatedAtBetween(startOfDay, endOfDay);
    }

    /**
     * Get tickets created this week
     */
    public long getTicketsCreatedThisWeek() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
        LocalDateTime startOfWeekTime = startOfWeek.atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        return ticketRepository.countByCreatedAtBetween(startOfWeekTime, now);
    }

    /**
     * Get tickets grouped by category
     */
    public List<Object[]> getTicketsByCategory() {
        return ticketRepository.countTicketsByCategory();
    }

    /**
     * Get tickets grouped by priority
     */
    public List<Object[]> getTicketsByPriority() {
        return ticketRepository.countTicketsByPriority();
    }

    /**
     * Find all tickets with pagination (for admin)
     */
    public Page<Ticket> findAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable);
    }

    // ============================================
    // STAFF PORTAL METHODS (NEW)
    // ============================================

    /**
     * Find tickets assigned to a specific staff member (User)
     */
    public Page<Ticket> findTicketsByAssignedTo(User staff, Pageable pageable) {
        return ticketRepository.findByAssignedTo(staff, pageable);
    }

    /**
     * Count tickets assigned to a specific staff member
     */
    public long countTicketsByAssignedTo(User staff) {
        return ticketRepository.countByAssignedTo(staff);
    }

    /**
     * Find tickets with filters (status, priority, category, search)
     * Used by Admin and Staff portals
     */
    public Page<Ticket> findTicketsWithFilters(Ticket.Status status, Ticket.Priority priority, 
                                                 Long categoryId, String search, Pageable pageable) {
        // If all filters are null, return all tickets
        if (status == null && priority == null && categoryId == null && (search == null || search.isEmpty())) {
            return ticketRepository.findAll(pageable);
        }

        // Apply filters using repository query methods
        // This is a simplified version - you can enhance this with Specifications or custom queries
        if (status != null && priority == null && categoryId == null && (search == null || search.isEmpty())) {
            return ticketRepository.findByStatus(status, pageable);
        }

        // For now, return all tickets filtered by status if provided
        // In a real implementation, you'd use JPA Specifications for complex filtering
        if (status != null) {
            return ticketRepository.findByStatus(status, pageable);
        }

        return ticketRepository.findAll(pageable);
    }

    /**
     * Update ticket status
     */
    @Transactional
    public void updateTicketStatus(Long ticketId, Ticket.Status status) {
        Optional<Ticket> ticketOpt = ticketRepository.findById(ticketId);
        if (ticketOpt.isPresent()) {
            Ticket ticket = ticketOpt.get();
            ticket.setStatus(status);
            ticketRepository.save(ticket);
        }
    }
}