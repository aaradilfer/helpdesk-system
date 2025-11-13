package com.helpdesk.repository;

import com.helpdesk.entity.ResponseTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResponseTemplateRepository extends JpaRepository<ResponseTemplate, Long> {
    List<ResponseTemplate> findByCreatedBy(String createdBy);
    List<ResponseTemplate> findByCreatedByOrCreatedBy(String createdBy, String adminCreatedBy);
}