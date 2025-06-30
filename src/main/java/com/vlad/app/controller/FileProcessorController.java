package com.vlad.app.controller;

import com.vlad.app.service.FileProcessorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class FileProcessorController {

    private final FileProcessorService fileProcessorService;

}
