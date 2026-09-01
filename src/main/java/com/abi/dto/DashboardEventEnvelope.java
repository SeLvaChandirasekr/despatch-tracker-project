package com.abi.dto;

import java.io.Serializable;
import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Data;

/*-----------------------------------------------------------------------
 * Wire envelope for every GET /dashboard/events message. eventId is deterministic
 * ("evt-" + externalApiTraceLogId, or a rule-derived id for ALERT) so redelivery of the
 * same underlying event carries the same id - the frontend dedupes on it.
 *
 * tenantId/tpaId/hospitalCode are filtering-only (DashboardSseBroadcaster matches a
 * subscriber's tenantId against them before sending) and are never serialized to clients.
 *---------------------------------------------------------------------*/
@Data
@Builder
public class DashboardEventEnvelope implements Serializable {

    private static final long serialVersionUID = 1L;

    private String eventId;

    // TRANSACTION_CREATED / TRANSACTION_STATUS_UPDATED / RETRY_STARTED / RETRY_COMPLETED /
    // QUERY_REPLY_RECEIVED / ALERT
    private String type;

    private Instant timestamp;

    // PREAUTH / CLAIMS
    private String module;

    private DashboardTransactionEvent transaction;

    private ModuleSummaryDTO summary;

    // populated only for type=ALERT
    private RecentAlertDTO alert;

    @JsonIgnore
    private Integer tenantId;

    @JsonIgnore
    private Integer tpaId;

    @JsonIgnore
    private String hospitalCode;

}
