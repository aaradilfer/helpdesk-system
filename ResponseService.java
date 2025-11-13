package com.helpdesk.service;

import com.helpdesk.entity.ResponseTemplate;
import com.helpdesk.repository.ResponseTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResponseService {

    private final ResponseTemplateRepository repository;

    public ResponseService(ResponseTemplateRepository repository) {
        this.repository = repository;
    }

    public List<ResponseTemplate> getAllResponses() {
        return repository.findAll();
    }

    public List<ResponseTemplate> getResponsesByUser(String username) {
        return repository.findByCreatedBy(username);
    }

    public List<ResponseTemplate> getUserAndAdminResponses(String username) {
        return repository.findByCreatedByOrCreatedBy(username, "admin");
    }

    public Optional<ResponseTemplate> findById(Long id) {
        return repository.findById(id);
    }

    public ResponseTemplate save(ResponseTemplate template) {
        return repository.save(template);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public List<ResponseTemplate> searchResponses(String query, String username) {
        List<ResponseTemplate> responses = getUserAndAdminResponses(username);
        return responses.stream()
                .filter(r -> r.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        r.getContent().toLowerCase().contains(query.toLowerCase()))
                .toList();
    }
}