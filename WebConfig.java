package com.helpdesk.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Root path handled by HomeController - DO NOT map here to avoid conflicts
        // registry.addViewController("/").setViewName("index");
        registry.addViewController("/auth/login").setViewName("auth/login");
        registry.addViewController("/business-admin/login").setViewName("business-admin/login");
    }
}