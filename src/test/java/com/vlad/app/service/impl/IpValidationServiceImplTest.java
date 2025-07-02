package com.vlad.app.service.impl;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.vlad.app.exception.BadRequestException;
import com.vlad.app.exception.ForbiddenException;
import com.vlad.app.exception.InternalServerException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.vlad.app.filter.AuditFilter.COUNTRY_CODE;
import static com.vlad.app.filter.AuditFilter.ISP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IpValidationServiceImplTest {
    private WireMockServer wireMockServer;
    private HttpServletRequest mockRequest;

    private IpValidationServiceImpl ipValidationService;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        configureFor("localhost", wireMockServer.port());

        RestTemplate restTemplate = new RestTemplateBuilder()
                .rootUri("http://localhost:8089")
                .build();

        ipValidationService = new IpValidationServiceImpl(restTemplate, wireMockServer.baseUrl());
        mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRemoteAddr()).thenReturn("000000000");
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Test
    void testPassingResponse() throws Exception {
        stubFor(any(urlPathMatching("/json/.*"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"status\":\"success\",\"countryCode\":\"CA\",\"isp\":\"Le Groupe Videotron Ltee\",\"org\":\"Videotron Ltee\",\"query\":\"24.48.0.1\"}")
                ));

        //validation passes
        Assertions.assertDoesNotThrow(() -> {
            ipValidationService.validate(mockRequest);
        });
        //audit attributes set
        Mockito.verify(mockRequest).setAttribute(ISP, "Le Groupe Videotron Ltee");
        Mockito.verify(mockRequest).setAttribute(COUNTRY_CODE, "CA");
    }

    @Test
    void testCountryBlocked() throws Exception {
        stubFor(any(urlPathMatching("/json/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"success\",\"countryCode\":\"ES\",\"isp\":\"Telefonica de Espana SAU\",\"org\":\"RIMA (Red IP Multi Acceso)\",\"query\":\"88.26.241.248\"}")
                ));

        Exception exception = assertThrows(ForbiddenException.class, () -> {
            ipValidationService.validate(mockRequest);
        });
        assertEquals("Country blocked ", exception.getMessage());
    }

    @Test void testOrganisationBlocked() throws Exception {
        stubFor(any(urlPathMatching("/json/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"success\",\"countryCode\":\"SG\",\"isp\":\"Amazon.com, Inc.\",\"org\":\"AWS EC2 (ap-southeast-1)\",\"query\":\"18.140.0.1\"}")
                ));
        Exception exception = assertThrows(ForbiddenException.class, () -> {
            ipValidationService.validate(mockRequest);
        });
        assertEquals("Organisation blocked ", exception.getMessage());
    }

    @Test
    void testOrganisationAndCountryBlocked() throws Exception {
        stubFor(any(urlPathMatching("/json/.*"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"status\":\"success\",\"country\":\"United States\",\"countryCode\":\"US\",\"isp\":\"Google LLC\",\"org\":\"Google Cloud (us-west1)\",\"as\":\"AS396982 Google LLC\",\"asname\":\"GOOGLE-CLOUD-PLATFORM\"}")
        ));
        Exception exception = assertThrows(ForbiddenException.class, () -> {
            ipValidationService.validate(mockRequest);
        });
        assertEquals("Country blocked Organisation blocked ", exception.getMessage());
    }

    @Test
    void testFailResponse() throws Exception {
        stubFor(any(urlPathMatching("/json/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"fail\",\"message\":\"reserved range\",\"query\":\"0.0.0.1\"}")
                ));
        Exception exception = assertThrows(BadRequestException.class, () -> {
            ipValidationService.validate(mockRequest);
        });
        assertEquals("reserved range", exception.getMessage());
    }

    @Test
    void testErrorResponse() throws Exception {
        stubFor(any(urlPathMatching("/json/.*"))
                .willReturn(aResponse()
                        .withStatus(500)
                ));
        Exception exception = assertThrows(InternalServerException.class, () -> {
            ipValidationService.validate(mockRequest);
        });
        assertEquals("500 Internal Server Error on GET request for \"http://localhost:8089/json/000000000\": [no body]", exception.getMessage());
    }
}