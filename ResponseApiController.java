package com.helpdesk.controller;

import com.helpdesk.entity.ResponseTemplate;
import com.helpdesk.service.ResponseService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/responses")
public class ResponseApiController {

    private final ResponseService responseService;

    public ResponseApiController(ResponseService responseService) {
        this.responseService = responseService;
    }

    @GetMapping
    public List<ResponseTemplate> getResponses(Authentication authentication) {
        String username = authentication.getName();
        return responseService.getUserAndAdminResponses(username);
    }

    @GetMapping("/search")
    public List<ResponseTemplate> searchResponses(@RequestParam String query, Authentication authentication) {
        String username = authentication.getName();
        return responseService.searchResponses(query, username);
    }

    @GetMapping("/{id}")
    public ResponseTemplate getResponse(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        ResponseTemplate template = responseService.findById(id)
                .orElseThrow(() -> new RuntimeException("Response template not found"));

        // Check permission
        if (!role.equals("ROLE_ADMIN") && !template.getCreatedBy().equals(username)) {
            throw new RuntimeException("Access denied");
        }

        return template;
    }
}