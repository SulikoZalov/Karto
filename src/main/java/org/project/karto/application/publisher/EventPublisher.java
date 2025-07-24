package org.project.karto.application.publisher;

import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import org.project.karto.domain.card.events.CashbackEvent;

@ApplicationScoped
public class EventPublisher {

    private final EventBus eventBus;

    EventPublisher(Instance<EventBus> eventBus) {
        this.eventBus = eventBus.get();
    }

    public void publish(CashbackEvent event) {
        eventBus.request("user.cashback", event);
    }
}