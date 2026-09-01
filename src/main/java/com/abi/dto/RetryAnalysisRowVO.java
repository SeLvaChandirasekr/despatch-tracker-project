package com.abi.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/*-----------------------------------------------------------------------
 * One row of the Retry Analysis Report - one retried case (SUBMIT chain, size > 1). See
 * ReportServiceImpl.buildRetryAnalysisReport().
 *---------------------------------------------------------------------*/
@Data
public class RetryAnalysisRowVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer tenantId;

    private String tenantName;

    // PREAUTH / CLAIMS
    private String module;

    private String referenceId;

    private int attemptCount;

    private Date firstAttemptTime;

    private Date lastAttemptTime;

    // SUCCESS / FAILURE - the chain's final outcome
    private String finalStatus;

    // SINGLE_RETRY_SUCCESS / MULTIPLE_RETRY_SUCCESS / FAILED_AFTER_RETRY
    private String outcome;

}
