package com.abi.event;

import org.springframework.context.ApplicationEvent;

import com.abi.entity.ExternalApiTraceLog;

import lombok.Getter;

/*-----------------------------------------------------------------------
 * Raised (not yet dispatched) from within ExternalApiTraceServiceImpl.log()'s
 * @Transactional method, right after the row is saved. previousStatus/parentTransactionId
 * are computed at write time from prior rows for the same case - they aren't stored on the
 * entity itself. DashboardTransactionEventListener picks this up AFTER_COMMIT.
 *---------------------------------------------------------------------*/
@Getter
public class TransactionPersistedEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private final ExternalApiTraceLog trace;
    private final String previousStatus;
    private final String parentTransactionId;

    public TransactionPersistedEvent(Object source, ExternalApiTraceLog trace, String previousStatus,
	    String parentTransactionId) {
	super(source);
	this.trace = trace;
	this.previousStatus = previousStatus;
	this.parentTransactionId = parentTransactionId;
    }

}
