package com.helpdesk.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

// @Component
@Order(1)
public class BusinessAdminSessionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String requestURI = httpRequest.getRequestURI();

        // Check if user is trying to access business admin protected routes
        if (requiresBusinessAdminAuth(requestURI)) {
            if (session == null || session.getAttribute("businessAdminLoggedIn") == null) {
                // Redirect to business admin login if not authenticated
                httpResponse.sendRedirect("/business-admin/login?error=session_expired");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean requiresBusinessAdminAuth(String requestURI) {
        // Define which routes require business admin session authentication
        return requestURI.startsWith("/dashboard") ||
                requestURI.startsWith("/tickets") ||
                requestURI.startsWith("/admin/reports") ||
                requestURI.startsWith("/reports");
    }
}