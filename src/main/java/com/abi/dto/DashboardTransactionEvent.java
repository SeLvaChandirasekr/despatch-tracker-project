package com.abi.dto;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

/*-----------------------------------------------------------------------
 * The "transaction" object inside a DashboardEventEnvelope - business-facing shape per
 * the dashboard's SSE contract. id/caseId are the business correlation id (reference_id),
 * never the raw trace-log row id.
 *---------------------------------------------------------------------*/
@Data
@Builder
public class DashboardTransactionEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String caseId;

    // Preauth / Claims
    private String module;

    // Inbound / Outbound / Query Reply
    private String flow;

    private String apiTpa;

    private String hospital;

    private String status;

    private String previousStatus;

    private boolean retry;

    // yyyy-MM-dd HH:mm
    private String timestamp;

}
