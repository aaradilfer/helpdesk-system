package com.helpdesk.config;

import com.helpdesk.entity.Category;
import com.helpdesk.entity.ResponseTemplate;
import com.helpdesk.entity.Staff;
import com.helpdesk.entity.User;
import com.helpdesk.entity.SupportTeamUser;
import com.helpdesk.entity.ArticleCategory;
import com.helpdesk.entity.SupportArticle;
import com.helpdesk.entity.PaymentTransaction;
import com.helpdesk.repository.CategoryRepository;
import com.helpdesk.repository.ResponseTemplateRepository;
import com.helpdesk.repository.StaffRepository;
import com.helpdesk.repository.UserRepository;
import com.helpdesk.repository.SupportTeamUserRepository;
import com.helpdesk.repository.ArticleCategoryRepository;
import com.helpdesk.repository.SupportArticleRepository;
import com.helpdesk.repository.PaymentTransactionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ResponseTemplateRepository responseRepository;
    private final CategoryRepository categoryRepository;
    private final StaffRepository staffRepository;
    private final SupportTeamUserRepository supportTeamUserRepository;
    private final ArticleCategoryRepository articleCategoryRepository;
    private final SupportArticleRepository supportArticleRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           ResponseTemplateRepository responseRepository,
                           CategoryRepository categoryRepository,
                           StaffRepository staffRepository,
                           SupportTeamUserRepository supportTeamUserRepository,
                           ArticleCategoryRepository articleCategoryRepository,
                           SupportArticleRepository supportArticleRepository,
                           PaymentTransactionRepository paymentTransactionRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.responseRepository = responseRepository;
        this.categoryRepository = categoryRepository;
        this.staffRepository = staffRepository;
        this.supportTeamUserRepository = supportTeamUserRepository;
        this.articleCategoryRepository = articleCategoryRepository;
        this.supportArticleRepository = supportArticleRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create admin user for User Management
        if (userRepository.findByEmail("admin@university.edu").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin@university.edu"); // Set username for Spring Security
            admin.setFullName("System Administrator");
            admin.setEmail("admin@university.edu");
            admin.setRole(User.UserRole.ADMIN);
            admin.setStatus(User.UserStatus.ACTIVE);
            admin.setEnabled(true); // Enable for Spring Security
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setIsDeleted(false);
            userRepository.save(admin);
            System.out.println("Created admin user for User Management: admin@university.edu/admin123");
        }

        // Create support staff users for Response Management
        if (userRepository.findByEmail("shanika@edufix.local").isEmpty()) {
            User shanika = new User();
            shanika.setUsername("shanika@edufix.local"); // Set username for Spring Security
            shanika.setFullName("Shanika Perera");
            shanika.setEmail("shanika@edufix.local");
            shanika.setRole(User.UserRole.STAFF);
            shanika.setStatus(User.UserStatus.ACTIVE);
            shanika.setEnabled(true); // Enable for Spring Security
            shanika.setPasswordHash(passwordEncoder.encode("staff123"));
            shanika.setIsDeleted(false);
            userRepository.save(shanika);
            System.out.println("Created shanika user for Response Management: shanika@edufix.local/staff123");
        }

        if (userRepository.findByEmail("roshan@edufix.local").isEmpty()) {
            User roshan = new User();
            roshan.setUsername("roshan@edufix.local"); // Set username for Spring Security
            roshan.setFullName("Roshan Silva");
            roshan.setEmail("roshan@edufix.local");
            roshan.setRole(User.UserRole.STAFF);
            roshan.setStatus(User.UserStatus.ACTIVE);
            roshan.setEnabled(true); // Enable for Spring Security
            roshan.setPasswordHash(passwordEncoder.encode("staff123"));
            roshan.setIsDeleted(false);
            userRepository.save(roshan);
            System.out.println("Created roshan user for Response Management: roshan@edufix.local/staff123");
        }

        // Create admin user for Admin Portal
        if (userRepository.findByUsername("admin") == null || userRepository.findByUsername("admin").isEmpty()) {
            User adminPortalUser = new User();
            adminPortalUser.setUsername("admin");
            adminPortalUser.setFirstName("System");
            adminPortalUser.setLastName("Administrator");
            adminPortalUser.setFullName("System Administrator");
            adminPortalUser.setEmail("admin@helpdesk.com");
            adminPortalUser.setPhoneNumber("(555) 000-0000");
            adminPortalUser.setRole(User.UserRole.ADMIN);
            adminPortalUser.setStatus(User.UserStatus.ACTIVE);
            adminPortalUser.setEnabled(true);
            adminPortalUser.setPasswordHash(passwordEncoder.encode("admin123"));
            adminPortalUser.setIsDeleted(false);
            userRepository.save(adminPortalUser);
            System.out.println("Created admin user for Admin Portal: admin/admin123");
        }

        // Create sample student users for Student Portal
        if (userRepository.findByUsername("student1") == null || userRepository.findByUsername("student1").isEmpty()) {
            User student1 = new User();
            student1.setUsername("student1");
            student1.setFirstName("Alice");
            student1.setLastName("Johnson");
            student1.setFullName("Alice Johnson"); // For compatibility with existing code
            student1.setEmail("alice.johnson@student.edu");
            student1.setPhoneNumber("(555) 111-2222");
            student1.setRole(User.UserRole.STUDENT);
            student1.setStatus(User.UserStatus.ACTIVE);
            student1.setEnabled(true);
            student1.setPasswordHash(passwordEncoder.encode("student123"));
            student1.setIsDeleted(false);
            userRepository.save(student1);
            System.out.println("Created student1 user for Student Portal");
        }

        if (userRepository.findByUsername("student2") == null || userRepository.findByUsername("student2").isEmpty()) {
            User student2 = new User();
            student2.setUsername("student2");
            student2.setFirstName("Bob");
            student2.setLastName("Williams");
            student2.setFullName("Bob Williams"); // For compatibility with existing code
            student2.setEmail("bob.williams@student.edu");
            student2.setPhoneNumber("(555) 222-3333");
            student2.setRole(User.UserRole.STUDENT);
            student2.setStatus(User.UserStatus.ACTIVE);
            student2.setEnabled(true);
            student2.setPasswordHash(passwordEncoder.encode("student123"));
            student2.setIsDeleted(false);
            userRepository.save(student2);
            System.out.println("Created student2 user for Student Portal");
        }

        if (userRepository.findByUsername("student3") == null || userRepository.findByUsername("student3").isEmpty()) {
            User student3 = new User();
            student3.setUsername("student3");
            student3.setFirstName("Carol");
            student3.setLastName("Martinez");
            student3.setFullName("Carol Martinez"); // For compatibility with existing code
            student3.setEmail("carol.martinez@student.edu");
            student3.setPhoneNumber("(555) 333-4444");
            student3.setRole(User.UserRole.STUDENT);
            student3.setStatus(User.UserStatus.ACTIVE);
            student3.setEnabled(true);
            student3.setPasswordHash(passwordEncoder.encode("student123"));
            student3.setIsDeleted(false);
            userRepository.save(student3);
            System.out.println("Created student3 user for Student Portal");
        }

        // Create sample response templates
        if (responseRepository.count() == 0) {
            LocalDateTime now = LocalDateTime.now();

            ResponseTemplate r1 = new ResponseTemplate();
            r1.setTitle("Password Reset Instructions");
            r1.setContent("Dear Student,\n\nTo reset your password, please visit the password reset page and follow the instructions sent to your registered email address.\n\nIf you continue to experience issues, please don't hesitate to contact us.\n\nBest regards,\nHelp Desk Team");
            r1.setCreatedBy("admin");
            r1.setCreatedAt(now);
            r1.setUpdatedAt(now);
            responseRepository.save(r1);

            ResponseTemplate r2 = new ResponseTemplate();
            r2.setTitle("Network Connectivity Issues");
            r2.setContent("Dear Student,\n\nFor network connectivity issues, please try the following steps:\n1. Restart your device\n2. Check if other devices can connect\n3. Verify your network settings\n4. Contact your internet service provider if the issue persists\n\nIf the problem continues, please provide more details about the error messages you're seeing.\n\nBest regards,\nHelp Desk Team");
            r2.setCreatedBy("admin");
            r2.setCreatedAt(now);
            r2.setUpdatedAt(now);
            responseRepository.save(r2);

            ResponseTemplate r3 = new ResponseTemplate();
            r3.setTitle("Course Registration Help");
            r3.setContent("Dear Student,\n\nFor course registration assistance, please:\n1. Check the course prerequisites\n2. Verify there are available seats\n3. Contact your academic advisor\n4. Visit the registrar's office if you encounter system errors\n\nPlease include your student ID and the specific course codes in your response.\n\nBest regards,\nHelp Desk Team");
            r3.setCreatedBy("shanika");
            r3.setCreatedAt(now);
            r3.setUpdatedAt(now);
            responseRepository.save(r3);

            ResponseTemplate r4 = new ResponseTemplate();
            r4.setTitle("Library Access Issues");
            r4.setContent("Dear Student,\n\nIf you're experiencing issues accessing library resources:\n1. Clear your browser cache and cookies\n2. Try accessing from a different browser\n3. Ensure you're using your student credentials\n4. Contact the library help desk for further assistance\n\nBest regards,\nHelp Desk Team");
            r4.setCreatedBy("roshan");
            r4.setCreatedAt(now);
            r4.setUpdatedAt(now);
            responseRepository.save(r4);

            System.out.println("Created sample response templates");
        }

        // Initialize categories for Report Creation
        if (categoryRepository.count() == 0) {
            Category cat1 = new Category("Technical Issues", "Hardware and software problems");
            categoryRepository.save(cat1);

            Category cat2 = new Category("Account Problems", "Login, password, and account access issues");
            categoryRepository.save(cat2);

            Category cat3 = new Category("Network Issues", "Internet connectivity and network problems");
            categoryRepository.save(cat3);

            Category cat4 = new Category("Course Related", "Course enrollment, materials, and academic issues");
            categoryRepository.save(cat4);

            Category cat5 = new Category("General Inquiry", "General questions and information requests");
            categoryRepository.save(cat5);

            Category cat6 = new Category("System Access", "Access to university systems and applications");
            categoryRepository.save(cat6);

            System.out.println("Created sample categories for Report Creation");
        }

        // Initialize staff for Report Creation
        if (staffRepository.count() == 0) {
            Staff staff1 = new Staff();
            staff1.setName("John Smith");
            staff1.setEmail("john.smith@university.edu");
            staff1.setDepartment("IT Support");
            staff1.setPhone("(555) 123-4567");
            staff1.setPosition("Senior Technician");
            staffRepository.save(staff1);

            Staff staff2 = new Staff();
            staff2.setName("Sarah Johnson");
            staff2.setEmail("sarah.johnson@university.edu");
            staff2.setDepartment("IT Support");
            staff2.setPhone("(555) 234-5678");
            staff2.setPosition("Help Desk Specialist");
            staffRepository.save(staff2);

            Staff staff3 = new Staff();
            staff3.setName("Mike Davis");
            staff3.setEmail("mike.davis@university.edu");
            staff3.setDepartment("Academic Support");
            staff3.setPhone("(555) 345-6789");
            staff3.setPosition("Academic Advisor");
            staffRepository.save(staff3);

            Staff staff4 = new Staff();
            staff4.setName("Lisa Wilson");
            staff4.setEmail("lisa.wilson@university.edu");
            staff4.setDepartment("Network Services");
            staff4.setPhone("(555) 456-7890");
            staff4.setPosition("Network Administrator");
            staffRepository.save(staff4);

            Staff staff5 = new Staff();
            staff5.setName("David Brown");
            staff5.setEmail("david.brown@university.edu");
            staff5.setDepartment("IT Support");
            staff5.setPhone("(555) 567-8901");
            staff5.setPosition("System Administrator");
            staffRepository.save(staff5);

            System.out.println("Created sample staff members for Report Creation");
        }

        // Create staff user for Staff Portal (Spring Security STAFF role)
        if (userRepository.findByUsername("staff1").isEmpty()) {
            User staffUser = new User();
            staffUser.setUsername("staff1");
            staffUser.setFirstName("John");
            staffUser.setLastName("Staff");
            staffUser.setFullName("John Staff");
            staffUser.setEmail("john.staff@helpdesk.com");
            staffUser.setPhoneNumber("(555) 111-1111");
            staffUser.setRole(User.UserRole.STAFF);
            staffUser.setStatus(User.UserStatus.ACTIVE);
            staffUser.setEnabled(true);
            staffUser.setPasswordHash(passwordEncoder.encode("staff123"));
            staffUser.setIsDeleted(false);
            userRepository.save(staffUser);
            System.out.println("Created staff user for Staff Portal: staff1/staff123");
        }

        // Create additional staff user
        if (userRepository.findByUsername("staff2").isEmpty()) {
            User staffUser2 = new User();
            staffUser2.setUsername("staff2");
            staffUser2.setFirstName("Sarah");
            staffUser2.setLastName("Support");
            staffUser2.setFullName("Sarah Support");
            staffUser2.setEmail("sarah.support@helpdesk.com");
            staffUser2.setPhoneNumber("(555) 222-2222");
            staffUser2.setRole(User.UserRole.STAFF);
            staffUser2.setStatus(User.UserStatus.ACTIVE);
            staffUser2.setEnabled(true);
            staffUser2.setPasswordHash(passwordEncoder.encode("staff123"));
            staffUser2.setIsDeleted(false);
            userRepository.save(staffUser2);
            System.out.println("Created staff user for Staff Portal: staff2/staff123");
        }

        // Create payment team user for Payment Portal
        if (userRepository.findByUsername("payment1").isEmpty()) {
            User paymentUser = new User();
            paymentUser.setUsername("payment1");
            paymentUser.setFirstName("Payment");
            paymentUser.setLastName("Team");
            paymentUser.setFullName("Payment Team Member");
            paymentUser.setEmail("payment@helpdesk.com");
            paymentUser.setPhoneNumber("(555) 333-3333");
            paymentUser.setRole(User.UserRole.STAFF); // Payment team members are STAFF
            paymentUser.setStatus(User.UserStatus.ACTIVE);
            paymentUser.setEnabled(true);
            paymentUser.setPasswordHash(passwordEncoder.encode("payment123"));
            paymentUser.setIsDeleted(false);
            userRepository.save(paymentUser);
            System.out.println("Created payment team user for Payment Portal: payment1/payment123");
        }

        // Create payment categories (using existing Category entity)
        if (categoryRepository.count() < 10) {
            // Add payment-specific categories
            String[] paymentCategories = {
                "Tuition Fee", "Library Fee", "Hostel Fee", 
                "Laboratory Fee", "Examination Fee", "Late Fee"
            };
            
            for (String catName : paymentCategories) {
                if (categoryRepository.findByName(catName).isEmpty()) {
                    Category category = new Category();
                    category.setName(catName);
                    category.setDescription("Payment category for " + catName);
                    category.setIsActive(true);
                    categoryRepository.save(category);
                    System.out.println("Created payment category: " + catName);
                }
            }
        }

        // ============================================
        // LECTURER PORTAL INITIALIZATION (NEW)
        // ============================================

        // Create lecturer user for Lecturer Portal
        if (supportTeamUserRepository.findByUsername("lecturer1").isEmpty()) {
            SupportTeamUser lecturer = new SupportTeamUser();
            lecturer.setUsername("lecturer1");
            lecturer.setFullName("Dr. John Lecturer");
            lecturer.setEmail("lecturer@university.edu");
            lecturer.setPasswordHash(passwordEncoder.encode("lecturer123"));
            lecturer.setIsActive(true);
            supportTeamUserRepository.save(lecturer);
            System.out.println("Created lecturer user for Lecturer Portal: lecturer1/lecturer123");
        }

        // Create article categories for Lecturer Portal
        if (articleCategoryRepository.count() == 0) {
            String[] articleCats = {
                "Getting Started", "Account Management", "Technical Support",
                "FAQ", "Troubleshooting", "How-To Guides"
            };
            
            for (String catName : articleCats) {
                ArticleCategory category = new ArticleCategory(catName, "Help articles about " + catName);
                articleCategoryRepository.save(category);
                System.out.println("Created article category: " + catName);
            }
        }

        // Create sample articles for Lecturer Portal
        if (supportArticleRepository.count() == 0) {
            ArticleCategory gettingStarted = articleCategoryRepository.findByName("Getting Started").orElse(null);
            ArticleCategory faq = articleCategoryRepository.findByName("FAQ").orElse(null);

            // Sample Article 1
            SupportArticle article1 = new SupportArticle();
            article1.setTitle("How to Reset Your Password");
            article1.setContent("<h3>Password Reset Guide</h3><p>Follow these steps to reset your password:</p><ol><li>Click on 'Forgot Password' link on the login page</li><li>Enter your registered email address</li><li>Check your email for reset instructions</li><li>Click the reset link and enter your new password</li></ol>");
            article1.setCategory(gettingStarted);
            article1.setKeywords("password, reset, forgot, login");
            article1.setStatus("Published");
            article1.setAuthor("lecturer1");
            article1.setViewCount(45);
            article1.setHelpfulCount(38);
            article1.setNotHelpfulCount(2);
            article1.setPublishedAt(LocalDateTime.now());
            supportArticleRepository.save(article1);

            // Sample Article 2
            SupportArticle article2 = new SupportArticle();
            article2.setTitle("Frequently Asked Questions");
            article2.setContent("<h3>Common Questions</h3><p><strong>Q: How do I access my student dashboard?</strong><br>A: Login using your credentials and click on 'Student Portal' from the home page.</p><p><strong>Q: Where can I view my tickets?</strong><br>A: Navigate to 'My Tickets' section in your dashboard.</p>");
            article2.setCategory(faq);
            article2.setKeywords("faq, questions, help");
            article2.setStatus("Published");
            article2.setAuthor("lecturer1");
            article2.setViewCount(120);
            article2.setHelpfulCount(95);
            article2.setNotHelpfulCount(10);
            article2.setPublishedAt(LocalDateTime.now());
            supportArticleRepository.save(article2);

            // Sample Article 3 (Draft)
            SupportArticle article3 = new SupportArticle();
            article3.setTitle("Advanced Troubleshooting Guide");
            article3.setContent("<h3>Advanced Tips</h3><p>This article is under construction...</p>");
            article3.setCategory(articleCategoryRepository.findByName("Troubleshooting").orElse(null));
            article3.setKeywords("troubleshooting, advanced, tips");
            article3.setStatus("Draft");
            article3.setAuthor("lecturer1");
            article3.setViewCount(0);
            article3.setHelpfulCount(0);
            article3.setNotHelpfulCount(0);
            supportArticleRepository.save(article3);

            System.out.println("Created 3 sample support articles");
        }

        // ============================================
        // PAYMENT PORTAL - PAYMENT TRANSACTIONS
        // ============================================
        if (paymentTransactionRepository.count() == 0) {
            System.out.println("Initializing Payment Transactions...");

            // Get payment categories
            Category tuitionFee = categoryRepository.findByName("Tuition Fee").orElse(null);
            Category libraryFee = categoryRepository.findByName("Library Fee").orElse(null);
            Category hostelFee = categoryRepository.findByName("Hostel Fee").orElse(null);
            Category labFee = categoryRepository.findByName("Laboratory Fee").orElse(null);
            Category examFee = categoryRepository.findByName("Examination Fee").orElse(null);

            // Sample Transaction 1 - Verified Tuition Payment
            PaymentTransaction txn1 = new PaymentTransaction();
            txn1.setTransactionNumber("TXN-2025-0001");
            txn1.setStudentName("Alice Johnson");
            txn1.setStudentId("STU001");
            txn1.setStudentEmail("alice.johnson@university.edu");
            txn1.setStudentPhone("+94 71 234 5678");
            txn1.setAmount(new BigDecimal("25000.00"));
            txn1.setCategory(tuitionFee);
            txn1.setPaymentMethod(PaymentTransaction.PaymentMethod.BANK_TRANSFER);
            txn1.setReferenceNumber("BNK2025001234");
            txn1.setDescription("First semester tuition fee payment");
            txn1.setStatus(PaymentTransaction.Status.VERIFIED);
            txn1.setVerified(true);
            txn1.setVerifiedBy("payment1");
            txn1.setVerifiedAt(LocalDateTime.now().minusDays(1));
            txn1.setLastModifiedBy("payment1");
            txn1.setCreatedAt(LocalDateTime.now().minusDays(5));
            txn1.setUpdatedAt(LocalDateTime.now().minusDays(1));
            paymentTransactionRepository.save(txn1);

            // Sample Transaction 2 - Pending Library Fee
            PaymentTransaction txn2 = new PaymentTransaction();
            txn2.setTransactionNumber("TXN-2025-0002");
            txn2.setStudentName("Bob Smith");
            txn2.setStudentId("STU002");
            txn2.setStudentEmail("bob.smith@university.edu");
            txn2.setStudentPhone("+94 77 345 6789");
            txn2.setAmount(new BigDecimal("5000.00"));
            txn2.setCategory(libraryFee);
            txn2.setPaymentMethod(PaymentTransaction.PaymentMethod.ONLINE);
            txn2.setReferenceNumber("ONL20250056789");
            txn2.setDescription("Library membership and book deposit");
            txn2.setStatus(PaymentTransaction.Status.PENDING);
            txn2.setVerified(false);
            txn2.setLastModifiedBy("system");
            txn2.setCreatedAt(LocalDateTime.now().minusHours(12));
            txn2.setUpdatedAt(LocalDateTime.now().minusHours(12));
            paymentTransactionRepository.save(txn2);

            // Sample Transaction 3 - Verified Hostel Fee
            PaymentTransaction txn3 = new PaymentTransaction();
            txn3.setTransactionNumber("TXN-2025-0003");
            txn3.setStudentName("Carol Davis");
            txn3.setStudentId("STU003");
            txn3.setStudentEmail("carol.davis@university.edu");
            txn3.setStudentPhone("+94 75 456 7890");
            txn3.setAmount(new BigDecimal("15000.00"));
            txn3.setCategory(hostelFee);
            txn3.setPaymentMethod(PaymentTransaction.PaymentMethod.CARD);
            txn3.setReferenceNumber("CARD987654321");
            txn3.setDescription("Hostel accommodation for semester 1");
            txn3.setStatus(PaymentTransaction.Status.VERIFIED);
            txn3.setVerified(true);
            txn3.setVerifiedBy("payment1");
            txn3.setVerifiedAt(LocalDateTime.now().minusHours(6));
            txn3.setLastModifiedBy("payment1");
            txn3.setCreatedAt(LocalDateTime.now().minusDays(2));
            txn3.setUpdatedAt(LocalDateTime.now().minusHours(6));
            paymentTransactionRepository.save(txn3);

            // Sample Transaction 4 - Pending Lab Fee
            PaymentTransaction txn4 = new PaymentTransaction();
            txn4.setTransactionNumber("TXN-2025-0004");
            txn4.setStudentName("David Wilson");
            txn4.setStudentId("STU004");
            txn4.setStudentEmail("david.wilson@university.edu");
            txn4.setAmount(new BigDecimal("3500.00"));
            txn4.setCategory(labFee);
            txn4.setPaymentMethod(PaymentTransaction.PaymentMethod.CASH);
            txn4.setDescription("Chemistry lab equipment deposit");
            txn4.setStatus(PaymentTransaction.Status.PENDING);
            txn4.setVerified(false);
            txn4.setLastModifiedBy("system");
            txn4.setCreatedAt(LocalDateTime.now().minusHours(3));
            txn4.setUpdatedAt(LocalDateTime.now().minusHours(3));
            paymentTransactionRepository.save(txn4);

            // Sample Transaction 5 - Escalated Exam Fee
            PaymentTransaction txn5 = new PaymentTransaction();
            txn5.setTransactionNumber("TXN-2025-0005");
            txn5.setStudentName("Emma Brown");
            txn5.setStudentId("STU005");
            txn5.setStudentEmail("emma.brown@university.edu");
            txn5.setStudentPhone("+94 71 567 8901");
            txn5.setAmount(new BigDecimal("7500.00"));
            txn5.setCategory(examFee);
            txn5.setPaymentMethod(PaymentTransaction.PaymentMethod.BANK_TRANSFER);
            txn5.setReferenceNumber("BNK2025009876");
            txn5.setDescription("Final examination fee - requires verification");
            txn5.setStatus(PaymentTransaction.Status.ESCALATED);
            txn5.setVerified(false);
            txn5.setLastModifiedBy("payment1");
            txn5.setCreatedAt(LocalDateTime.now().minusDays(3));
            txn5.setUpdatedAt(LocalDateTime.now().minusHours(10));
            paymentTransactionRepository.save(txn5);

            // Sample Transaction 6 - Rejected Payment
            PaymentTransaction txn6 = new PaymentTransaction();
            txn6.setTransactionNumber("TXN-2025-0006");
            txn6.setStudentName("Frank Miller");
            txn6.setStudentId("STU006");
            txn6.setStudentEmail("frank.miller@university.edu");
            txn6.setAmount(new BigDecimal("2000.00"));
            txn6.setCategory(libraryFee);
            txn6.setPaymentMethod(PaymentTransaction.PaymentMethod.CHEQUE);
            txn6.setReferenceNumber("CHQ123456");
            txn6.setDescription("Library late fee payment - cheque bounced");
            txn6.setStatus(PaymentTransaction.Status.REJECTED);
            txn6.setVerified(false);
            txn6.setVerifiedBy("payment1");
            txn6.setVerifiedAt(LocalDateTime.now().minusHours(8));
            txn6.setLastModifiedBy("payment1");
            txn6.setCreatedAt(LocalDateTime.now().minusDays(4));
            txn6.setUpdatedAt(LocalDateTime.now().minusHours(8));
            paymentTransactionRepository.save(txn6);

            System.out.println("Created 6 sample payment transactions");
            System.out.println("  - 2 Verified (TXN-2025-0001, TXN-2025-0003)");
            System.out.println("  - 2 Pending (TXN-2025-0002, TXN-2025-0004)");
            System.out.println("  - 1 Escalated (TXN-2025-0005)");
            System.out.println("  - 1 Rejected (TXN-2025-0006)");
            System.out.println("  Total Amount: Rs. 58,000.00");
        }
    }
}