package com.abi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.abi.sse.DashboardSseBroadcaster;
import com.abi.util.ClaimBookConstants;

@RestController
public class DashboardSseController extends ClaimBookConstants {

    @Autowired
    private DashboardSseBroadcaster dashboardSseBroadcaster;

    @GetMapping(path = REQUEST_MAPPING_DASHBOARD_EVENTS, produces = CONTENT_TYPE_TEXT_EVENT_STREAM)
    public SseEmitter subscribe(@RequestParam(required = false) Integer tenantId,
	    @RequestParam(required = false) String hospitalId, @RequestParam(required = false) Integer tpaId,
	    @RequestParam(required = false) String module) {
	return dashboardSseBroadcaster.subscribe(tenantId, hospitalId, tpaId, module);
    }

}
