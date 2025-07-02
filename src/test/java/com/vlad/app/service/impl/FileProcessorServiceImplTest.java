package com.vlad.app.service.impl;

import com.vlad.app.model.PersonContent;
import com.vlad.app.service.FileProcessorService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;


import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileProcessorServiceImplTest {

    FileProcessorService fileProcessorService = new FileProcessorServiceImpl();

    @Test
    void process() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                getClass().getClassLoader().getResourceAsStream("fileProcessorServiceImplTest/test.txt")
        );

        List<PersonContent> personContents = fileProcessorService.processFile(file);
        assertNotNull(personContents);
        assertEquals(3, personContents.size());
        assertEquals("John Smith", personContents.get(0).getName());
    }

    @Test
    void processWithEmptyLines() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                getClass().getClassLoader().getResourceAsStream("fileProcessorServiceImplTest/test_empty_lines.txt")
        );

        List<PersonContent> personContents = fileProcessorService.processFile(file);
        assertNotNull(personContents);
        assertEquals(3, personContents.size());
        assertEquals("John Smith", personContents.get(0).getName());
    }

    @Test
    void processWithMalformedLines() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                getClass().getClassLoader().getResourceAsStream("fileProcessorServiceImplTest/test_malformed_lines.txt")
        );

        List<PersonContent> personContents = fileProcessorService.processFile(file);
        assertNotNull(personContents);
        assertEquals(2, personContents.size());
        assertEquals("John Smith", personContents.get(0).getName());
    }

    @Test
    void generateJsonFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                getClass().getClassLoader().getResourceAsStream("fileProcessorServiceImplTest/test.txt")
        );

        List<PersonContent> personContents = fileProcessorService.processFile(file);
        File jsonFile = fileProcessorService.generateTransportFile(personContents);

        assertNotNull(jsonFile);
        assertTrue(jsonFile.exists());

    }

    @Test
    void generateJsonFileEmptyInput() throws Exception {

        File jsonFile = fileProcessorService.generateTransportFile(null);

        assertNotNull(jsonFile);
        assertTrue(jsonFile.exists());
    }

}