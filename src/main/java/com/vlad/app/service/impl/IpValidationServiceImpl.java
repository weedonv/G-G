package com.vlad.app.service.impl;

import com.vlad.app.exception.BadRequestException;
import com.vlad.app.exception.ForbiddenException;
import com.vlad.app.exception.InternalServerException;
import com.vlad.app.model.api.IpDetailsResponse;
import com.vlad.app.service.IpValidationService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static com.vlad.app.filter.AuditFilter.COUNTRY_CODE;
import static com.vlad.app.filter.AuditFilter.ISP;

@Service
public class IpValidationServiceImpl implements IpValidationService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public IpValidationServiceImpl(RestTemplate restTemplate,
                                   @Value("${ip.api.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public void validate(HttpServletRequest request) {
        String clientIpAddress = request.getRemoteAddr();
        IpDetailsResponse ipDetailsResponse;

        try {
            ipDetailsResponse = restTemplate.getForObject(baseUrl + "/json/{clientIpAddress}?fields=58882", IpDetailsResponse.class, clientIpAddress);
        } catch (RestClientException e) {
            throw new InternalServerException(e.getMessage());
        }

        if (ipDetailsResponse != null) {
            setAuditAttributes(request, ipDetailsResponse);
            if (ipDetailsResponse.getStatus().equals("success")) {
                boolean isBlockedCountry = isBlockedCountry(ipDetailsResponse);
                boolean isBlockedOrganisation = isBlockedOrganisation(ipDetailsResponse);
                if (isBlockedCountry || isBlockedOrganisation) {
                    String message = "";
                    if (isBlockedCountry) {
                        message += "Country blocked ";
                    }
                    if (isBlockedOrganisation) {
                        message += "Organisation blocked ";
                    }
                    throw new ForbiddenException(message);
                }
            }
            if (ipDetailsResponse.getStatus().equals("fail")) {
                throw new BadRequestException(ipDetailsResponse.getMessage());
            }
        } else {
            throw new InternalServerException("Unknown error");
        }
    }

    private void setAuditAttributes(HttpServletRequest request, IpDetailsResponse ipDetailsResponse) {
        request.setAttribute(ISP, StringUtils.isBlank(ipDetailsResponse.getIsp()) ? "unknown" : ipDetailsResponse.getIsp());
        request.setAttribute(COUNTRY_CODE, StringUtils.isBlank(ipDetailsResponse.getCountryCode()) ? "unknown" : ipDetailsResponse.getCountryCode());
    }

    private boolean isBlockedCountry(IpDetailsResponse ipDetailsResponse) {
        return StringUtils.equalsAnyIgnoreCase(ipDetailsResponse.getCountryCode(), "CN", "ES", "US");
    }

    private boolean isBlockedOrganisation(IpDetailsResponse ipDetailsResponse) {
        return StringUtils.containsAnyIgnoreCase(ipDetailsResponse.getOrg(), "Google Cloud", "Microsoft Azure Cloud", "AWS");
    }
}
