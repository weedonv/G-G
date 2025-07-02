package com.vlad.app.service;

import com.vlad.app.model.PersonContent;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

public interface FileProcessorService {
    List<PersonContent> processFile(MultipartFile file);

    File generateTransportFile(List<PersonContent> personContentList);
}
