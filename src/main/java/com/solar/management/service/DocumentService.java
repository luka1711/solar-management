package com.solar.management.service;

import com.solar.management.model.Project;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface DocumentService {
    ResponseEntity<?> generateProjectOfferPdf(String userInitiated, Long projectId);
}
