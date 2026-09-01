package com.abi.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class DashboardKpiDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // Logical-transaction level (one count per retry chain, using its latest attempt's
    // status) - not raw external_api_trace_log row counts.
    private int totalTransactions;

    private int success;

    private int failure;

    private int queryReply;

    private int retryTransactions;

    private int totalRetryAttempts;

    private ComparisonDTO comparisonToPreviousPeriod = new ComparisonDTO();

}
