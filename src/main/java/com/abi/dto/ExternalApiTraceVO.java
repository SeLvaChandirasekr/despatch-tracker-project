package com.abi.dto;

import java.util.Date;

import lombok.Data;

/*-----------------------------------------------------------------------
 * Input to ExternalApiTraceService.log(...) - one instance per outbound call to a
 * TPA/payor, regardless of functionality (member validation, submission, query
 * reply, portal status, upload, OTP...). requestPayload/responsePayload accept any
 * object; the service JSON-serializes them before persisting.
 *---------------------------------------------------------------------*/
@Data
public class ExternalApiTraceVO {

    private Integer tenantId;

    private Integer tpaId;

    private String hospitalCode;

    private String functionality;

    private String requestType;

    private String actionType;

    private String referenceId;

    private String alNumber;

    private String apiUrl;

    private String httpMethod;

    private String status;

    private String businessStatus;

    private Integer httpStatus;

    private boolean retrySubmission;

    private Integer attemptNo;

    private String payorCaseRefId;

    private Object requestPayload;

    private Object responsePayload;

    private String errorResponse;

    private String exceptionMessage;

    private String remarks;

    private Object sourcePayload;
    private Date requestedTime;

    private Date responseReceivedTime;

}
