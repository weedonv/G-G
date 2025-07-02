package com.vlad.app.controller;

import com.vlad.app.exception.BadRequestException;
import com.vlad.app.model.PersonContent;
import com.vlad.app.service.FileProcessorService;
import com.vlad.app.service.IpValidationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class FileProcessorController {

    @Value("${ip.validation.enabled}")
    private boolean ipValidationEnabled;

    @Value("${validation.enabled}")
    private boolean validationEnabled;

    private final FileProcessorService fileProcessorService;
    private final IpValidationService ipValidationService;

    @PostMapping(value ="/file/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Resource> processFile(@RequestParam("file") MultipartFile file, HttpServletRequest request) {

        if (ipValidationEnabled) {
            ipValidationService.validate(request);
        }

        if (validationEnabled) {
            validate(file);
        }

        List<PersonContent> personContentList = fileProcessorService.processFile(file);
        File transportJsonFile = fileProcessorService.generateTransportFile(personContentList);

        // requirements were to download json file rather than display json output
        Resource resource = new FileSystemResource(transportJsonFile);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"OutcomeFile.json\"")
                .body(resource);
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is missing");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".txt")) {
            throw new BadRequestException("Only .txt files are supported");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("text/plain")) {
            throw new BadRequestException("File must be a plain text file (text/plain)");
        }
    }
}
