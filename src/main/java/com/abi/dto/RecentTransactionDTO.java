package com.abi.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class RecentTransactionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // Business correlation id (reference_id) - never the raw trace-log row id.
    private String id;

    private String caseId;

    private String module;

    private String flow;

    private String apiTpa;

    private String status;

    // yyyy-MM-dd HH:mm
    private String timestamp;

}
