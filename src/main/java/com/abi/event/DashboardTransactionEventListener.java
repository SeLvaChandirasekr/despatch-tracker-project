package com.abi.event;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.abi.dto.DashboardEventEnvelope;
import com.abi.dto.DashboardTransactionEvent;
import com.abi.dto.ModuleSummaryDTO;
import com.abi.dto.RecentAlertDTO;
import com.abi.entity.ExternalApiTraceLog;
import com.abi.entity.MasterPayor;
import com.abi.repository.MasterPayorRepository;
import com.abi.service.DashboardEventPublisher;
import com.abi.service.DashboardService;

/*-----------------------------------------------------------------------
 * Fires only after ExternalApiTraceServiceImpl.log()'s transaction actually commits - if it
 * rolls back, this never runs, so no dashboard event can be sent for a row that was never
 * persisted. A publish failure here is logged and swallowed: it must never affect the
 * business write, which has already committed by the time this runs.
 *---------------------------------------------------------------------*/
@Component
public class DashboardTransactionEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardTransactionEventListener.class);

    private static final String ACTION_TYPE_QUERY_REPLY = "QUERY_REPLY";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILURE = "FAILURE";
    private static final double HIGH_FAILURE_RATE_THRESHOLD_PCT = 50.0;
    private static final double HIGH_RETRY_RATE_THRESHOLD_PCT = 30.0;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private DashboardEventPublisher dashboardEventPublisher;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private MasterPayorRepository masterPayorRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransactionPersisted(TransactionPersistedEvent event) {
	ExternalApiTraceLog trace = event.getTrace();
	try {
	    String flowType = resolveFlowType(trace.getActionType());
	    String eventType = resolveEventType(trace, event.getPreviousStatus());
	    String apiTpaName = resolveTpaName(trace.getTpaId());

	    DashboardTransactionEvent transaction = DashboardTransactionEvent.builder()
		    .id(trace.getReferenceId())
		    .caseId(trace.getReferenceId())
		    .module(trace.getFunctionality())
		    .flow(ACTION_TYPE_QUERY_REPLY.equals(trace.getActionType()) ? "QUERY_REPLY" : flowType)
		    .apiTpa(apiTpaName)
		    .hospital(trace.getHospitalCode())
		    .status(trace.getStatus())
		    .previousStatus(event.getPreviousStatus())
		    .retry(trace.isRetrySubmission())
		    .timestamp(TIMESTAMP_FORMATTER.format(Instant.now().atZone(ZoneId.systemDefault())))
		    .build();

	    ModuleSummaryDTO summary = dashboardService.computeModuleSummary(trace.getTenantId(),
		    trace.getHospitalCode(), trace.getTpaId(), trace.getFunctionality());

	    DashboardEventEnvelope envelope = DashboardEventEnvelope.builder()
		    .eventId("evt-" + trace.getExternalApiTraceLogId())
		    .type(eventType)
		    .timestamp(Instant.now())
		    .module(trace.getFunctionality())
		    .transaction(transaction)
		    .summary(summary)
		    .tenantId(trace.getTenantId())
		    .tpaId(trace.getTpaId())
		    .hospitalCode(trace.getHospitalCode())
		    .build();

	    dashboardEventPublisher.publish(envelope);

	    publishAlertIfThresholdCrossed(trace, summary);
	} catch (Exception e) {
	    LOGGER.error("Failed to publish dashboard event for trace {} - business transaction already "
		    + "committed, dashboard will self-correct on the next summary read",
		    trace == null ? null : trace.getExternalApiTraceLogId(), e);
	}
    }

    // Best-effort: reuses the same thresholds as DashboardServiceImpl's recentAlerts, applied to
    // the module summary already computed above - not a full monitoring subsystem, just an
    // immediate push for the same condition the next /dashboard/summary call would also surface.
    private void publishAlertIfThresholdCrossed(ExternalApiTraceLog trace, ModuleSummaryDTO summary) {
	int total = summary.getInbound().getTotal() + summary.getOutbound().getTotal()
		+ summary.getQueryReply().getTotal();
	int failure = summary.getInbound().getFailure() + summary.getOutbound().getFailure()
		+ summary.getQueryReply().getFailure();

	RecentAlertDTO triggered = null;
	if (total > 0 && (failure * 100.0 / total) >= HIGH_FAILURE_RATE_THRESHOLD_PCT) {
	    triggered = alert("alert-highfailure-" + trace.getFunctionality(), "warning", "High Failure Rate",
		    trace.getFunctionality() + " at " + Math.round(failure * 100.0 / total) + "%");
	} else if (summary.getTotal() > 0
		&& (summary.getRetry().getRetriedTransactions() * 100.0 / summary.getTotal()) >= HIGH_RETRY_RATE_THRESHOLD_PCT) {
	    triggered = alert("alert-highretry-" + trace.getFunctionality(), "warning", "High Retry Rate",
		    trace.getFunctionality() + " at "
			    + Math.round(summary.getRetry().getRetriedTransactions() * 100.0 / summary.getTotal())
			    + "%");
	}
	if (triggered == null) {
	    return;
	}

	DashboardEventEnvelope alertEnvelope = DashboardEventEnvelope.builder().eventId(triggered.getId())
		.type("ALERT").timestamp(Instant.now()).module(trace.getFunctionality()).alert(triggered)
		.tenantId(trace.getTenantId()).tpaId(trace.getTpaId()).hospitalCode(trace.getHospitalCode()).build();
	dashboardEventPublisher.publish(alertEnvelope);
    }

    private RecentAlertDTO alert(String id, String severity, String title, String detail) {
	RecentAlertDTO dto = new RecentAlertDTO();
	dto.setId(id);
	dto.setSeverity(severity);
	dto.setTitle(title);
	dto.setDetail(detail);
	dto.setTime("just now");
	return dto;
    }

    private String resolveTpaName(Integer tpaId) {
	if (tpaId == null) {
	    return null;
	}
	return masterPayorRepository.findAll().stream().filter(p -> tpaId.equals(p.getMasterPayorId()))
		.map(MasterPayor::getTpaName).findFirst().orElse("TPA " + tpaId);
    }

    // Kept in sync with the same rule baked into vw_dashboard_transaction_events's flow_type column.
    private String resolveFlowType(String actionType) {
	if ("SUBMIT".equals(actionType) || ACTION_TYPE_QUERY_REPLY.equals(actionType)
		|| "DOCUMENT_UPLOAD".equals(actionType)) {
	    return "OUTBOUND";
	}
	return "INBOUND";
    }

    private String resolveEventType(ExternalApiTraceLog trace, String previousStatus) {
	if (ACTION_TYPE_QUERY_REPLY.equals(trace.getActionType())) {
	    return "QUERY_REPLY_RECEIVED";
	}
	if (trace.isRetrySubmission()) {
	    boolean terminal = STATUS_SUCCESS.equalsIgnoreCase(trace.getStatus())
		    || STATUS_FAILURE.equalsIgnoreCase(trace.getStatus());
	    return terminal ? "RETRY_COMPLETED" : "RETRY_STARTED";
	}
	return previousStatus == null ? "TRANSACTION_CREATED" : "TRANSACTION_STATUS_UPDATED";
    }

}
