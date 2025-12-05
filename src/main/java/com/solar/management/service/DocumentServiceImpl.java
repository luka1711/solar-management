package com.solar.management.service;

import com.lowagie.text.DocumentException;
import com.solar.management.exception.ProjectException;
import com.solar.management.model.Project;
import com.solar.management.model.User;
import com.solar.management.repository.ProjectRepository;
import com.solar.management.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.ByteArrayOutputStream;


import java.time.LocalDateTime;

@Service
public class DocumentServiceImpl implements DocumentService {


    private final SpringTemplateEngine templateEngine;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectServiceImpl projectService;

    @Autowired
    public DocumentServiceImpl(SpringTemplateEngine templateEngine, UserRepository userRepository, ProjectRepository projectRepository, ProjectServiceImpl projectService) {
        this.templateEngine = templateEngine;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.projectService = projectService;
    }

    @Override
    public ResponseEntity<?> generateProjectOfferPdf(String userInitiated, Long projectId) {
        User requester = userRepository.findByEmail(userInitiated)
                .orElseThrow(() -> new DocumentException("User initiated is not found."));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new DocumentException("Project with id: " + projectId + " doesn't exist"));

        if(project.getExpectedProductionKwhYr() == null) throw new DocumentException("Document can't be prepared as calculation is not performed");


        if (projectService.validateProjectAccess(requester, project)){
            String document = prepareHtml(project);
            byte[] pdf = preparePdf(document);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=offer-" + projectId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        }
        else throw new DocumentException("User doesn't have project access");

    }

    byte[] preparePdf(String html){
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(baos);
            builder.run();

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }

    String prepareHtml(Project project) {
        Context context = new Context();
        context.setVariable("companyName", project.getCompany().getName());
        context.setVariable("customerName", project.getCustomer().getFullName());
        context.setVariable("customerAddress", project.getCustomer().getAddress());
        context.setVariable("date", LocalDateTime.now());
        context.setVariable("totalPeakPower", project.getTotalPeakPower());
        context.setVariable("panelCount", project.getTotalNumberOfPanels());
        context.setVariable("panelWatt", project.getPanelWatt());
        context.setVariable("roofArea", project.getRoofArea());
        context.setVariable("yearly", project.getExpectedProductionKwhYr());
        context.setVariable("monthly", project.getEstimatedMonthlyKwh());
        context.setVariable("daily", project.getEstimatedDailyKwh());
        return templateEngine.process("templates/project-offer-pdf", context);

    }
}
