package com.abi.entity;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

/*-----------------------------------------------------------------------
 * Read-only mapping over the vw_dashboard_transaction_events DB view, which derives
 * flowType/previousStatus/parentTransactionId from external_api_trace_log via window
 * functions (see the view definition) rather than storing them as columns. Used only
 * for dashboard aggregation reads - never saved to.
 *---------------------------------------------------------------------*/
@Entity
@Table(name = "vw_dashboard_transaction_events")
@Immutable
@Data
public class DashboardTransactionRow implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "external_api_trace_log_id")
    private Long externalApiTraceLogId;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "case_id")
    private String caseId;

    @Column(name = "tenant_id")
    private Integer tenantId;

    @Column(name = "hospital_id")
    private String hospitalId;

    @Column(name = "tpa_id")
    private Integer tpaId;

    // PREAUTH / CLAIMS
    @Column(name = "module")
    private String module;

    // INBOUND / OUTBOUND
    @Column(name = "flow_type")
    private String flowType;

    @Column(name = "action_type")
    private String actionType;

    @Column(name = "request_type")
    private String requestType;

    @Column(name = "status")
    private String status;

    @Column(name = "previous_status")
    private String previousStatus;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "retry_flag")
    private boolean retryFlag;

    @Column(name = "retry_attempt_number")
    private Integer retryAttemptNumber;

    @Column(name = "parent_transaction_id")
    private String parentTransactionId;

    @Column(name = "elapsed_time_ms")
    private Long elapsedTimeMs;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

}
