package com.abi.sse;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.abi.dto.DashboardEventEnvelope;

/*-----------------------------------------------------------------------
 * Holds every open GET /dashboard/events connection and each one's subscribe-time filter
 * (tenantId/hospitalId/tpaId/module - caller-supplied, no per-user enforcement, matching
 * /dashboard/summary). broadcast(...) is the local fan-out target for both the in-memory
 * publisher today and, later, a RabbitMQ consumer - neither business services nor
 * DashboardTransactionEventListener need to know which one is wired up.
 *---------------------------------------------------------------------*/
@Component
public class DashboardSseBroadcaster {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardSseBroadcaster.class);

    private final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe(Integer tenantId, String hospitalId, Integer tpaId, String module) {
	SseEmitter emitter = new SseEmitter(0L);
	Subscription subscription = new Subscription(emitter, tenantId, hospitalId, tpaId, module);
	subscriptions.add(subscription);

	emitter.onCompletion(() -> subscriptions.remove(subscription));
	emitter.onTimeout(() -> {
	    subscriptions.remove(subscription);
	    emitter.complete();
	});
	emitter.onError(ex -> subscriptions.remove(subscription));

	try {
	    emitter.send(SseEmitter.event().name("connected").data("connected"));
	} catch (IOException e) {
	    subscriptions.remove(subscription);
	}
	return emitter;
    }

    public void broadcast(DashboardEventEnvelope event) {
	for (Subscription subscription : subscriptions) {
	    if (!subscription.matches(event)) {
		continue;
	    }
	    try {
		subscription.emitter.send(SseEmitter.event().id(event.getEventId()).name(event.getType()).data(event));
	    } catch (IOException | IllegalStateException e) {
		LOGGER.warn("Dropping SSE subscriber after failed send: {}", e.getMessage());
		subscription.emitter.complete();
		subscriptions.remove(subscription);
	    }
	}
    }

    @Scheduled(fixedRate = 15000)
    public void heartbeat() {
	for (Subscription subscription : subscriptions) {
	    try {
		subscription.emitter.send(SseEmitter.event().comment("keep-alive"));
	    } catch (IOException | IllegalStateException e) {
		subscription.emitter.complete();
		subscriptions.remove(subscription);
	    }
	}
    }

    private static final class Subscription {
	private final SseEmitter emitter;
	private final Integer tenantId;
	private final String hospitalId;
	private final Integer tpaId;
	private final String module;

	private Subscription(SseEmitter emitter, Integer tenantId, String hospitalId, Integer tpaId, String module) {
	    this.emitter = emitter;
	    this.tenantId = tenantId;
	    this.hospitalId = hospitalId;
	    this.tpaId = tpaId;
	    this.module = module;
	}

	private boolean matches(DashboardEventEnvelope event) {
	    if (tenantId != null && !tenantId.equals(event.getTenantId())) {
		return false;
	    }
	    if (hospitalId != null && !hospitalId.equals(event.getHospitalCode())) {
		return false;
	    }
	    if (tpaId != null && !tpaId.equals(event.getTpaId())) {
		return false;
	    }
	    if (module != null && !module.equalsIgnoreCase(event.getModule())) {
		return false;
	    }
	    return true;
	}
    }

}
