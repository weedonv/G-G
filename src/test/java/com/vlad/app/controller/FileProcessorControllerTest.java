package com.vlad.app.controller;

import com.vlad.app.entity.AuditLog;
import com.vlad.app.model.PersonContent;
import com.vlad.app.repository.AuditLogRepository;
import com.vlad.app.service.FileProcessorService;
import com.vlad.app.service.IpValidationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.io.File;

import java.util.List;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(FileProcessorController.class)
class FileProcessorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileProcessorService fileProcessorService;

    @MockitoBean
    private IpValidationService ipValidationService;

    @MockitoBean
    private AuditLogRepository auditLogRepository;

    @Test
    void processFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                getClass().getClassLoader().getResourceAsStream("test.txt")
        );

        List<PersonContent> personContent = List.of(new PersonContent("uuid", "id", "name", "likes", "transport", 30.3, 100.1));
        when(fileProcessorService.processFile(file)).thenReturn(personContent);
        when(fileProcessorService.generateTransportFile(personContent)).thenReturn(File.createTempFile("OutcomeFile", ".json"));

        mockMvc.perform(multipart("/api/file/process")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        //filter interaction
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void processFileValidationFailedExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txtx",
                MediaType.TEXT_PLAIN_VALUE,
                getClass().getClassLoader().getResourceAsStream("test.txtx")
        );

        mockMvc.perform(multipart("/api/file/process")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
        //filter interaction
        verify(auditLogRepository).save(any(AuditLog.class));
    }

    @Test
    void processFileValidationFailedMediaType() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.APPLICATION_JSON_VALUE,
                getClass().getClassLoader().getResourceAsStream("test.txt")
        );

        mockMvc.perform(multipart("/api/file/process")
                        .file(file)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
        //filter interaction
        verify(auditLogRepository).save(any(AuditLog.class));
    }

}