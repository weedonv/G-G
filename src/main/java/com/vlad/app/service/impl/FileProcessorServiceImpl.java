package com.vlad.app.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vlad.app.exception.InternalServerException;
import com.vlad.app.model.PersonContent;
import com.vlad.app.model.TransportDetail;
import com.vlad.app.service.FileProcessorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileProcessorServiceImpl implements FileProcessorService {
    private static final Logger logger = LoggerFactory.getLogger(FileProcessorServiceImpl.class);

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public List<PersonContent> processFile(MultipartFile file) {

        List<PersonContent> people = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (!line.trim().isEmpty()) {
                    line = line.trim();
                    if (line.endsWith("\\n")) {
                        line = line.substring(0, line.length() - 2);
                    }

                    String[] parts = line.split("\\|");
                    if (parts.length == 7) {
                            PersonContent person = new PersonContent(
                                    parts[0],
                                    parts[1],
                                    parts[2],
                                    parts[3],
                                    parts[4],
                                    Double.parseDouble(parts[5]),
                                    Double.parseDouble(parts[6])
                            );
                            people.add(person);
                    } else {
                        logger.warn("Invalid line @ {}: {}", lineNumber, line);
                    }
                }
            }
        } catch (IOException e) {
            throw new InternalServerException(e.getMessage());
        }

        return people;

    }

    @Override
    public File generateTransportFile(List<PersonContent> personContentList) {
        if (personContentList == null) {
            personContentList = new ArrayList<>();
        }

        List<TransportDetail> transportDetailList = personContentList
                .stream()
                .map(pc -> new TransportDetail(pc.getName(), pc.getTransport(), pc.getTopSpeed()))
                .toList();

        try {
            File tempFile = File.createTempFile("OutcomeFile", ".json");
            objectMapper.writeValue(tempFile, transportDetailList);
            return tempFile;
        } catch (IOException e) {
            throw new InternalServerException(e.getMessage());
        }
    }
}
