package com.abi.service;

import com.abi.dto.DashboardEventEnvelope;

/*-----------------------------------------------------------------------
 * Single seam every transaction-writing path publishes dashboard events through, so no
 * individual service needs its own dashboard-counter logic. InMemoryDashboardEventPublisher
 * is the only implementation today (single instance, broadcasts straight to local SSE
 * connections). If ClaimBook runs multiple instances later, a RabbitMqDashboardEventPublisher
 * can be swapped in via a Spring profile/conditional bean - callers of publish(...) don't change.
 *---------------------------------------------------------------------*/
public interface DashboardEventPublisher {

    void publish(DashboardEventEnvelope event);

}
