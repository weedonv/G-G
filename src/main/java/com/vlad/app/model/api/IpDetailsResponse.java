package com.vlad.app.model.api;

import lombok.Data;

@Data
public class IpDetailsResponse {
    private String status;
    private String message;
    private String query;
    private String countryCode;
    private String isp;
    private String org;
}
