package com.abi.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.abi.dto.ExternalApiTraceVO;
import com.abi.entity.ExternalApiTraceLog;
import com.abi.event.TransactionPersistedEvent;
import com.abi.repository.ExternalApiTraceRepository;
import com.abi.service.ExternalApiTraceService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ExternalApiTraceServiceImpl implements ExternalApiTraceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalApiTraceServiceImpl.class);

    @Autowired
    private ExternalApiTraceRepository externalApiTraceRepository;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional
    public void log(ExternalApiTraceVO trace) {
	if (trace == null) {
	    return;
	}
	try {
	    ObjectMapper mapper = new ObjectMapper();

	    // Prior attempts for this same case+functionality+actionType, oldest first - gives us
	    // previousStatus (last prior row's status) and parentTransactionId (first prior row's id,
	    // only meaningful when this new row is itself a retry) for the dashboard event, without
	    // needing a stored transactionId or an upsert - log() stays a plain insert.
	    List<ExternalApiTraceLog> priorAttempts = (trace.getReferenceId() != null
		    && trace.getFunctionality() != null && trace.getActionType() != null)
			    ? externalApiTraceRepository.findByReferenceIdAndFunctionalityAndActionTypeOrderByRequestedTimeAsc(
				    trace.getReferenceId(), trace.getFunctionality(), trace.getActionType())
			    : Collections.emptyList();
	    String previousStatus = priorAttempts.isEmpty() ? null
		    : priorAttempts.get(priorAttempts.size() - 1).getStatus();
	    String parentTransactionId = (trace.isRetrySubmission() && !priorAttempts.isEmpty())
		    ? String.valueOf(priorAttempts.get(0).getExternalApiTraceLogId())
		    : null;

	    ExternalApiTraceLog entity = new ExternalApiTraceLog();
	    entity.setTenantId(trace.getTenantId());
	    entity.setTpaId(trace.getTpaId());
	    entity.setHospitalCode(trace.getHospitalCode());
	    entity.setFunctionality(trace.getFunctionality());
	    entity.setRequestType(trace.getRequestType());
	    entity.setActionType(trace.getActionType());
	    entity.setReferenceId(trace.getReferenceId());
	    entity.setAlNumber(trace.getAlNumber());
	    entity.setApiUrl(trace.getApiUrl());
	    entity.setHttpMethod(trace.getHttpMethod());
	    entity.setStatus(trace.getStatus());
	    entity.setBusinessStatus(trace.getBusinessStatus());
	    entity.setHttpStatus(trace.getHttpStatus());
	    entity.setRetrySubmission(trace.isRetrySubmission());
	    entity.setAttemptNo(trace.getAttemptNo());
	    entity.setPayorCaseRefId(trace.getPayorCaseRefId());
	    entity.setErrorResponse(trace.getErrorResponse());
	    entity.setExceptionMessage(trace.getExceptionMessage());
	    entity.setRemarks(trace.getRemarks());
	    entity.setRequestedTime(trace.getRequestedTime());
	    entity.setResponseReceivedTime(trace.getResponseReceivedTime());
	    entity.setElapsedTimeMs(
		    trace.getRequestedTime() != null && trace.getResponseReceivedTime() != null
			    ? trace.getResponseReceivedTime().getTime() - trace.getRequestedTime().getTime()
			    : null);
	    entity.setCreatedDate(new Date());

	    if (trace.getRequestPayload() != null) {
		entity.setRequestPayload(mapper.writeValueAsString(trace.getRequestPayload()));
	    }
	    if (trace.getResponsePayload() != null) {
		entity.setResponsePayload(mapper.writeValueAsString(trace.getResponsePayload()));
	    }

	    if (trace.getSourcePayload() != null) {
		    entity.setSourcePayload(mapper.writeValueAsString(trace.getSourcePayload()));
		}
	    ExternalApiTraceLog saved = externalApiTraceRepository.save(entity);

	    applicationEventPublisher.publishEvent(
		    new TransactionPersistedEvent(this, saved, previousStatus, parentTransactionId));
	} catch (Exception e) {
	    LOGGER.error("Error while logging external API trace", e);
	}
    }

}
