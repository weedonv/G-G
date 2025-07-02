package com.vlad.app.service;

import jakarta.servlet.http.HttpServletRequest;

public interface IpValidationService {
    void validate(HttpServletRequest request);
}
