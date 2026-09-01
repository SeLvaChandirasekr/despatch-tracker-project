package com.abi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.abi.dto.ExternalApiTraceVO;
import com.abi.service.ExternalApiTraceService;
import com.abi.util.ClaimBookConstants;

/*-----------------------------------------------------------------------
 * Single write path for every Preauth/Claims transaction the dashboard tracks - whatever
 * created/processes those transactions calls this once per attempt (module, flowType-driving
 * actionType, status, retrySubmission/attemptNo for retries, etc.). Persists via
 * ExternalApiTraceService.log(), which publishes the dashboard event after commit.
 *---------------------------------------------------------------------*/
@RestController
public class TraceIngestController extends ClaimBookConstants {

    @Autowired
    private ExternalApiTraceService externalApiTraceService;

    @PostMapping(path = REQUEST_MAPPING_TRACE_EVENTS, produces = CONTENT_TYPE_APPLICATION_JSON)
    public ResponseEntity<Void> logTransaction(@RequestBody ExternalApiTraceVO trace) {
	externalApiTraceService.log(trace);
	return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

}
