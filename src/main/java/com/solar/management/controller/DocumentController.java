package com.solar.management.controller;

import com.solar.management.model.User;
import com.solar.management.service.DocumentService;
import com.solar.management.service.DocumentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/project")
public class DocumentController {

    private final DocumentServiceImpl documentService;
    @Autowired
    public DocumentController(DocumentServiceImpl documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/{projectId}/offer")
    ResponseEntity<?> generateDocument(@PathVariable Long projectId, Authentication authentication) {
        //this need to be done
        String userInitiated = authentication.getName();

        return documentService.generateProjectOfferPdf(userInitiated, projectId);
    }
}
