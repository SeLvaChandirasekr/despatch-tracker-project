package com.abi.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/*-----------------------------------------------------------------------
 * One row of the Failure Analysis page - one FAILURE event on a SUBMIT or QUERY_REPLY
 * attempt (root-cause view, not deduped per case - a case retried multiple times with
 * different failure reasons shows up once per failed attempt). See
 * DashboardServiceImpl.getFailureAnalysis().
 *---------------------------------------------------------------------*/
@Data
public class FailureAnalysisRowVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String referenceId;

    private Integer tenantId;

    private String tenantName;

    private Integer payerTpaId;

    private String payerTpaName;

    // PREAUTH / CLAIMS
    private String module;

    // Submission / Query Reply
    private String stage;

    private String reason;

    private Date occurredAt;

}
