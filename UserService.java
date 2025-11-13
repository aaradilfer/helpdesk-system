package com.helpdesk.service;

import com.helpdesk.entity.User;
import com.helpdesk.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(User user, String rawPassword) {
        logger.info("=== STARTING USER CREATION ===");
        logger.info("Attempting to create user with email: {}", user.getEmail());

        try {
            // Check for duplicate email
            if (userRepository.existsByEmailAndIsDeletedFalse(user.getEmail())) {
                logger.warn("User with email '{}' already exists", user.getEmail());
                throw new RuntimeException("User with email '" + user.getEmail() + "' already exists");
            }

            // Create a completely new User instance to avoid detached entity issues
            User newUser = new User();
            newUser.setFullName(user.getFullName());
            newUser.setEmail(user.getEmail());
            newUser.setRole(user.getRole());
            newUser.setStatus(user.getStatus() != null ? user.getStatus() : User.UserStatus.ACTIVE);
            newUser.setPasswordHash(passwordEncoder.encode(rawPassword));
            newUser.setIsDeleted(false);

            logger.info("Saving new user to database...");

            // Save and flush to database immediately
            User savedUser = userRepository.saveAndFlush(newUser);

            logger.info("User saved with ID: {}", savedUser.getId());
            logger.info("Verifying user persistence...");

            // Verify the save by querying fresh from database
            Optional<User> verifiedUser = userRepository.findById(savedUser.getId());
            if (verifiedUser.isPresent()) {
                logger.info("✓ User verification successful: ID={}, Email={}",
                        verifiedUser.get().getId(), verifiedUser.get().getEmail());
                return verifiedUser.get();
            } else {
                logger.error("✗ User was not found in database after save!");
                throw new RuntimeException("User was not persisted to database");
            }

        } catch (RuntimeException e) {
            logger.error("✗ Error creating user {}: {}", user.getEmail(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("✗ Unexpected error creating user {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Error creating user: " + e.getMessage(), e);
        } finally {
            logger.info("=== USER CREATION PROCESS ENDED ===");
        }
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findByIsDeletedFalse();
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsDeleted(true);
            user.setStatus(User.UserStatus.INACTIVE);
            userRepository.save(user);
            logger.info("User with ID {} soft deleted", id);
        }
    }

    @Transactional(readOnly = true)
    public List<User> searchUsers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.searchActiveUsers(searchTerm.trim());
    }

    @Transactional
    public User toggleUserStatus(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            User.UserStatus newStatus = user.getStatus() == User.UserStatus.ACTIVE ?
                    User.UserStatus.INACTIVE : User.UserStatus.ACTIVE;
            user.setStatus(newStatus);
            User updatedUser = userRepository.save(user);
            logger.info("User {} status changed to {}", user.getEmail(), newStatus);
            return updatedUser;
        }
        throw new RuntimeException("User not found with id: " + id);
    }

    @Transactional
    public User updateUserWithPassword(Long id, User updatedUser, String newPassword) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Check if email is being changed and if it already exists
            if (!user.getEmail().equals(updatedUser.getEmail())) {
                if (userRepository.existsByEmailAndIsDeletedFalse(updatedUser.getEmail())) {
                    throw new RuntimeException("User with email '" + updatedUser.getEmail() + "' already exists");
                }
            }

            user.setFullName(updatedUser.getFullName());
            user.setEmail(updatedUser.getEmail());
            user.setRole(updatedUser.getRole());
            user.setStatus(updatedUser.getStatus());

            if (newPassword != null && !newPassword.trim().isEmpty()) {
                user.setPasswordHash(passwordEncoder.encode(newPassword.trim()));
                logger.info("Password updated for user: {}", user.getEmail());
            }

            User savedUser = userRepository.save(user);
            logger.info("User updated successfully: {}", user.getEmail());
            return savedUser;
        }
        throw new RuntimeException("User not found with id: " + id);
    }

    @Transactional(readOnly = true)
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailAndIsDeletedFalse(email);
    }

    // NEW METHODS for Student Portal Integration

    /**
     * Get currently authenticated user from Spring Security context
     */
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .filter(user -> !user.isDeleted())
                .orElse(null);
    }

    /**
     * Register a new student user
     */
    @Transactional
    public User registerStudent(User user, String rawPassword) {
        logger.info("=== STARTING STUDENT REGISTRATION ===");
        logger.info("Attempting to register student: {}", user.getEmail());

        try {
            // Check for duplicate username
            if (user.getUsername() != null && userRepository.existsByUsername(user.getUsername())) {
                logger.warn("Username '{}' already exists", user.getUsername());
                throw new RuntimeException("Username '" + user.getUsername() + "' already exists");
            }

            // Check for duplicate email
            if (userRepository.existsByEmail(user.getEmail())) {
                logger.warn("Email '{}' already exists", user.getEmail());
                throw new RuntimeException("Email '" + user.getEmail() + "' already exists");
            }

            // Create new student user
            User newStudent = new User();
            newStudent.setUsername(user.getUsername());
            newStudent.setEmail(user.getEmail());
            newStudent.setFirstName(user.getFirstName());
            newStudent.setLastName(user.getLastName());
            newStudent.setPhoneNumber(user.getPhoneNumber());
            
            // Set fullName for compatibility with existing system
            newStudent.setFullName(user.getFirstName() + " " + user.getLastName());
            
            newStudent.setRole(User.UserRole.STUDENT);
            newStudent.setStatus(User.UserStatus.ACTIVE);
            newStudent.setEnabled(true);
            newStudent.setIsDeleted(false);
            newStudent.setPasswordHash(passwordEncoder.encode(rawPassword));

            logger.info("Saving new student to database...");
            User savedStudent = userRepository.saveAndFlush(newStudent);

            logger.info("✓ Student registered successfully: ID={}, Username={}",
                    savedStudent.getId(), savedStudent.getUsername());
            return savedStudent;

        } catch (RuntimeException e) {
            logger.error("✗ Error registering student: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("✗ Unexpected error registering student: {}", e.getMessage(), e);
            throw new RuntimeException("Error registering student: " + e.getMessage(), e);
        } finally {
            logger.info("=== STUDENT REGISTRATION PROCESS ENDED ===");
        }
    }

    /**
     * Get user by username
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsernameAndIsDeletedFalse(username);
    }

    /**
     * Check if username exists
     */
    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsernameAndIsDeletedFalse(username);
    }

    /**
     * Check if email exists
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmailAndIsDeletedFalse(email);
    }

    /**
     * Get all students
     */
    @Transactional(readOnly = true)
    public List<User> getAllStudents() {
        return userRepository.findByRoleAndIsDeletedFalseAndEnabledTrue(User.UserRole.STUDENT);
    }

    /**
     * Get all staff members
     */
    @Transactional(readOnly = true)
    public List<User> getAllStaff() {
        return userRepository.findByRoleAndIsDeletedFalseAndEnabledTrue(User.UserRole.STAFF);
    }

    // ============================================
    // ADMIN PORTAL METHODS (NEW)
    // ============================================

    /**
     * Find all users with pagination
     */
    @Transactional(readOnly = true)
    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findByIsDeletedFalse(pageable);
    }

    /**
     * Search users by name, email, or username
     */
    @Transactional(readOnly = true)
    public Page<User> searchUsers(String search, Pageable pageable) {
        return userRepository.searchUsers(search, pageable);
    }

    /**
     * Find users by role
     */
    @Transactional(readOnly = true)
    public Page<User> findUsersByRole(User.Role role, Pageable pageable) {
        // Convert Role enum to UserRole enum
        User.UserRole userRole = null;
        if (role == User.Role.ADMIN) userRole = User.UserRole.ADMIN;
        else if (role == User.Role.STAFF) userRole = User.UserRole.STAFF;
        else if (role == User.Role.STUDENT) userRole = User.UserRole.STUDENT;
        
        return userRepository.findByRoleAndIsDeletedFalse(userRole, pageable);
    }

    // toggleUserStatus method already exists in line 124, using that one for admin portal

    /**
     * Get top 5 students by ticket count
     */
    @Transactional(readOnly = true)
    public List<Object[]> getTop5StudentsByTicketCount() {
        return userRepository.getTop5StudentsByTicketCount();
    }

    /**
     * Create staff user (for admin portal)
     */
    @Transactional
    public User createStaffUser(User user, String rawPassword) {
        // Check for duplicate username
        if (user.getUsername() != null && userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check for duplicate email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User newStaff = new User();
        newStaff.setUsername(user.getUsername());
        newStaff.setEmail(user.getEmail());
        newStaff.setFirstName(user.getFirstName());
        newStaff.setLastName(user.getLastName());
        newStaff.setPhoneNumber(user.getPhoneNumber());
        newStaff.setFullName(user.getFirstName() + " " + user.getLastName());
        newStaff.setRole(User.UserRole.STAFF);
        newStaff.setStatus(User.UserStatus.ACTIVE);
        newStaff.setEnabled(true);
        newStaff.setIsDeleted(false);
        newStaff.setPasswordHash(passwordEncoder.encode(rawPassword));

        return userRepository.save(newStaff);
    }

    /**
     * Find user by username (for admin portal)
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }


}