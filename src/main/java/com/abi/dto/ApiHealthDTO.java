package com.abi.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ApiHealthDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private double overallPct;

    // EXCELLENT / HEALTHY / DEGRADED / DOWN
    private String overallLabel;

    private List<EndpointHealthDTO> endpoints = new ArrayList<>();

}
