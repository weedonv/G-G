package com.vlad.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransportDetail {
    private String name;
    private String transport;
    private double topSpeed;
}
