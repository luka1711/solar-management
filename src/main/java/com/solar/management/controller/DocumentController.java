package com.solar.management.controller;

import com.solar.management.model.User;
import com.solar.management.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/document")
public class DocumentController {

    private final DocumentService documentService;
    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    ResponseEntity<?> generateDocument(Authentication authentication) {
        //this need to be done
        String userInitiated = authentication.getName();
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
