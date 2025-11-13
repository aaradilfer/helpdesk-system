package com.helpdesk.service;

import com.helpdesk.entity.Staff;
import com.helpdesk.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StaffService {

    @Autowired
    private StaffRepository staffRepository;

    // CRUD Operations
    public Staff createStaff(Staff staff) {
        if (staffRepository.existsByEmail(staff.getEmail())) {
            throw new RuntimeException("Staff with email '" + staff.getEmail() + "' already exists");
        }
        return staffRepository.save(staff);
    }

    public Optional<Staff> getStaffById(Long id) {
        return staffRepository.findById(id);
    }

    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    public List<Staff> getActiveStaff() {
        return staffRepository.findByIsActiveTrue();
    }

    public Staff updateStaff(Staff staff) {
        return staffRepository.save(staff);
    }

    public void deleteStaff(Long id) {
        Optional<Staff> staffOpt = staffRepository.findById(id);
        if (staffOpt.isPresent()) {
            Staff staff = staffOpt.get();
            staff.setIsActive(false); // Soft delete
            staffRepository.save(staff);
        }
    }

    public Optional<Staff> getStaffByEmail(String email) {
        return staffRepository.findByEmail(email);
    }

    public List<Staff> getStaffByDepartment(String department) {
        return staffRepository.findByDepartmentAndIsActiveTrue(department);
    }

    public List<Object[]> getStaffWithTicketCount() {
        return staffRepository.findStaffWithTicketCount();
    }

    public List<String> getAllDepartments() {
        return staffRepository.findAllDepartments();
    }
}