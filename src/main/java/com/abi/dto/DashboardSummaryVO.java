package com.abi.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

/*-----------------------------------------------------------------------
 * One dashboard tile (Outbound / Query Reply / Inbound), aggregated in Java from the
 * ExternalApiTraceLog rows matching a given functionality+actionType+date range.
 * requestTypeCounts keys are the raw requestType values (INITIAL/ENHANCEMENT/DISCHARGE...).
 *---------------------------------------------------------------------*/
@Data
public class DashboardSummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int submitted;

    private int success;

    private int failure;

    private Map<String, Integer> requestTypeCounts = new HashMap<>();

    private int firstAttemptSuccess;

    private int firstAttemptFailure;

    private int multiAttemptSuccess;

    private int multiAttemptFailure;

    private int retryCount;

    // only populated for actionType=PORTAL_STATUS - counts of businessStatus values
    // (Approve/Query/Reject/Cancel) on top of the SUCCESS/FAILURE call-outcome counts above
    private Map<String, Integer> businessStatusCounts = new HashMap<>();

}
