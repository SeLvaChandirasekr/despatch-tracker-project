package com.abi.entity;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

/*-----------------------------------------------------------------------
 * Single append-only audit log for every outbound call this service makes to a
 * TPA/payor - member validation, preauth/claim submission, query reply, portal
 * status polling, document upload, OTP, etc. Unlike CaseCurrentStatus/CaseApiHistory
 * (which track the resolved state of a preauth/claim case), this table has no
 * "current status" concept - it's a flat trail, one row per attempt, for auditing.
 *---------------------------------------------------------------------*/
@Entity
@Table(name = "external_api_trace_log")
@Data
public class ExternalApiTraceLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "external_api_trace_log_id_seq", sequenceName = "external_api_trace_log_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "external_api_trace_log_id_seq")
    @Column(name = "external_api_trace_log_id")
    private Long externalApiTraceLogId;

    @Column(name = "tenant_id")
    private Integer tenantId;

    @Column(name = "tpa_id")
    private Integer tpaId;

    @Column(name = "hospital_code")
    private String hospitalCode;

    // e.g. PREAUTH, CLAIMS, MEMBER_VALIDATION, PORTAL_STATUS, QUERY_REPLY, DOCUMENT_UPLOAD, OTP, AUTH_TOKEN
    @Column(name = "functionality")
    private String functionality;

    // e.g. INITIAL, ENHANCEMENT, DISCHARGE, REVISION, CLAIMS, MEMBER_VALIDATION, PORTAL_STATUS...
    @Column(name = "request_type")
    private String requestType;

    // e.g. SUBMIT, VALIDATE, POLL, QUERY_REPLY, UPLOAD, CANCEL, OTP_SEND, OTP_VERIFY
    @Column(name = "action_type")
    private String actionType;

    // CB preauth/claim id, when this call is tied to a case
    @Column(name = "reference_id")
    private String referenceId;

    @Column(name = "al_number")
    private String alNumber;

    @Column(name = "api_url")
    private String apiUrl;

    @Column(name = "http_method")
    private String httpMethod;

    // SUCCESS / FAILURE - outcome of the HTTP call itself
    @Column(name = "status")
    private String status;

    // payor's business decision, e.g. Approve/Query/Reject/Cancel - only set for PORTAL_STATUS actionType
    @Column(name = "business_status")
    private String businessStatus;

    @Column(name = "http_status")
    private Integer httpStatus;

    @Column(name = "retry_submission")
    private boolean retrySubmission;

    @Column(name = "attempt_no")
    private Integer attemptNo;

    @Column(name = "payor_case_ref_id")
    private String payorCaseRefId;

    @Column(name = "request_payload", columnDefinition = "TEXT")
    private String requestPayload;
    

    @Column(name = "source_payload", columnDefinition = "TEXT")
    private String sourcePayload;


    @Column(name = "response_payload", columnDefinition = "TEXT")
    private String responsePayload;

    @Column(name = "error_response", columnDefinition = "TEXT")
    private String errorResponse;

    @Column(name = "exception_message")
    private String exceptionMessage;

    @Column(name = "remarks")
    private String remarks;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "requested_time")
    private Date requestedTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "response_received_time")
    private Date responseReceivedTime;

    @Column(name = "elapsed_time_ms")
    private Long elapsedTimeMs;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_date")
    private Date createdDate;

}
