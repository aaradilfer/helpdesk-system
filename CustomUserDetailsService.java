package com.helpdesk.security;

import com.helpdesk.entity.User;
import com.helpdesk.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;
    @Value("${support.staff.username:}")
    private String supportStaffUsername;

    @Value("${support.staff.password:}")
    private String supportStaffPassword;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        logger.debug("Loading user by identifier (email or username): {}", identifier);

        // Try to find by email first (for existing projects - User Management, Response Management)
        Optional<User> userOpt = userRepository.findByEmail(identifier);

        // If not found by email, try to find by username (for Student Portal)
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByUsername(identifier);
        }

        if (userOpt.isEmpty()) {
            // Fallback to application.properties support staff credentials
            if (supportStaffUsername != null && !supportStaffUsername.isBlank()
                    && identifier.equalsIgnoreCase(supportStaffUsername)) {
                logger.debug("Authenticating support staff from properties: {}", identifier);
                List<GrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_STAFF")
                );

                // Encode the configured password for comparison; Spring compares encoded stored value
                String encoded = new BCryptPasswordEncoder().encode(supportStaffPassword != null ? supportStaffPassword : "");

                return new org.springframework.security.core.userdetails.User(
                        supportStaffUsername,
                        encoded,
                        true, true, true, true,
                        authorities
                );
            }

            logger.warn("User not found with identifier: {}", identifier);
            throw new UsernameNotFoundException("User not found with identifier: " + identifier);
        }

        User user = userOpt.get();

        // Check if user is deleted
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            logger.warn("User account is deleted: {}", identifier);
            throw new UsernameNotFoundException("User account is deleted: " + identifier);
        }

        // Check if user is inactive
        if (user.getStatus() == User.UserStatus.INACTIVE) {
            logger.warn("User account is inactive: {}", identifier);
            throw new UsernameNotFoundException("User account is inactive: " + identifier);
        }

        // Additional check for student accounts - must be enabled
        if (user.getRole() == User.UserRole.STUDENT && user.getEnabled() != null && !user.getEnabled()) {
            logger.warn("Student account is disabled: {}", identifier);
            throw new UsernameNotFoundException("Student account is disabled: " + identifier);
        }

        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        logger.debug("User authenticated successfully: {}", identifier);

        // Return the appropriate identifier (username for students, email for others)
        String principalName = user.getUsername() != null && !user.getUsername().isEmpty() 
                ? user.getUsername() 
                : user.getEmail();

        return new org.springframework.security.core.userdetails.User(
                principalName,
                user.getPasswordHash(),
                true, true, true, true,
                authorities
        );
    }
}