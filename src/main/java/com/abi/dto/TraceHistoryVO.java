package com.abi.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

/*-----------------------------------------------------------------------
 * One row of the Case History / Polling History timeline, mapped from ExternalApiTraceLog.
 * Deliberately excludes requestPayload/responsePayload/errorResponse - those are full JSON
 * blobs meant for the audit trail, not for a list view; fetch a single trace row directly
 * if the raw payload is needed.
 *---------------------------------------------------------------------*/
@Data
public class TraceHistoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long externalApiTraceLogId;

    private String functionality;

    private String requestType;

    private String actionType;

    private String status;

    private String businessStatus;

    private Integer attemptNo;

    private boolean retrySubmission;

    private String remarks;

    private String exceptionMessage;

    // human-readable step label, e.g. "Submission Failed", "Submission Retried",
    // "Submission Succeeded", "Portal Status Polled" - built from actionType/status/retrySubmission
    private String eventLabel;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private Date requestedTime;

    @JsonDeserialize(using = CustomDateTimeDeserializer.class)
    @JsonSerialize(using = CustomDateTimeSerializer.class)
    private Date responseReceivedTime;

    private Long elapsedTimeMs;

}
