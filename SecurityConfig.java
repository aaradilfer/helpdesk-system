package com.helpdesk.config;

import com.helpdesk.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${business.admin.username:admin}")
    private String businessAdminUsername;

    @Value("${business.admin.password:admin123}")
    private String businessAdminPassword;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider());

        http
                .authorizeHttpRequests(authz -> authz
                        // Public pages - INDEX AND HOME (MUST BE FIRST!)
                        .requestMatchers("/", "/index", "/home").permitAll()
                        
                        // Login and registration pages
                        .requestMatchers("/login", "/register").permitAll()
                        .requestMatchers("/auth/login", "/auth/**").permitAll()
                        .requestMatchers("/business-admin/**").permitAll()
                        .requestMatchers("/support-staff/**").permitAll()
                        
                        // Static resources
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", 
                                "/favicon.ico", "/error").permitAll()
                        
                        // Business Admin dashboard routes (session-based)
                        .requestMatchers("/dashboard", "/api/dashboard/stats").permitAll()
                        .requestMatchers("/admin/reports/**").permitAll()
                        
                        // Payment Portal routes (session-based authentication)
                        .requestMatchers("/payment/**").permitAll()
                        
                        // Lecturer Portal routes (session-based authentication)
                        .requestMatchers("/support/login", "/support/logout").permitAll()
                        .requestMatchers("/support/articles/public/**").permitAll()
                        .requestMatchers("/support/**").permitAll()
                        
                        // Admin Portal routes (Spring Security ADMIN role)
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        
                        // Student Portal routes (Spring Security STUDENT role)
                        .requestMatchers("/student/**").hasRole("STUDENT")
                        
                        // Staff routes
                        .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN")
                        
                        // Common ticket routes (session-based for Business Admin, or Spring Security authenticated)
                        .requestMatchers("/tickets/**").permitAll()
                        
                        // User Management routes
                        .requestMatchers("/users/**").authenticated()
                        
                        // All other requests are PERMITTED (changed from authenticated)
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .defaultSuccessUrl("/dashboard", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/")
                        .deleteCookies("JSESSIONID")
                        .invalidateHttpSession(true)
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService responseManagementUserDetailsService() {
        UserDetails staffUser = User.builder()
                .username("shanika")
                .password(passwordEncoder().encode("staff123"))
                .roles("STAFF")
                .build();

        UserDetails adminUser = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("admin123"))
                .roles("ADMIN", "STAFF")
                .build();

        UserDetails roshanUser = User.builder()
                .username("roshan")
                .password(passwordEncoder().encode("staff123"))
                .roles("STAFF")
                .build();

        return new InMemoryUserDetailsManager(staffUser, adminUser, roshanUser);
    }
}