package com.abi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abi.dto.DashboardEventEnvelope;
import com.abi.service.DashboardEventPublisher;
import com.abi.sse.DashboardSseBroadcaster;

/*-----------------------------------------------------------------------
 * Single-instance implementation: publish = broadcast to this JVM's own SSE connections.
 * A future multi-instance RabbitMqDashboardEventPublisher would instead send to an
 * exchange, with a @RabbitListener consumer calling the same DashboardSseBroadcaster.
 *---------------------------------------------------------------------*/
@Service
public class InMemoryDashboardEventPublisher implements DashboardEventPublisher {

    @Autowired
    private DashboardSseBroadcaster dashboardSseBroadcaster;

    @Override
    public void publish(DashboardEventEnvelope event) {
	dashboardSseBroadcaster.broadcast(event);
    }

}
