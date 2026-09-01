package com.abi.dto;

import java.io.Serializable;

import lombok.Data;

// Inbound/Outbound bucket - attempt-level counts within a module.
@Data
public class FlowSummaryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int total;

    private int success;

    private int failure;

}
