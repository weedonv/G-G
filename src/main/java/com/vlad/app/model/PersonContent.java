package com.vlad.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PersonContent {
    private String uuid;
    private String id;
    private String name;
    private String likes;
    private String transport;
    private double averageSpeed;
    private double topSpeed;
}
