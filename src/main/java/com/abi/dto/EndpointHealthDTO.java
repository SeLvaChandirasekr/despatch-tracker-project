package com.abi.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class EndpointHealthDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    // UP / DEGRADED / DOWN
    private String status;

    // null when DOWN or no successful calls in the window
    private Long responseTimeMs;

    private long lastCheckedSec;

}
