package org.project.karto.application.publisher;

import io.smallrye.mutiny.Uni;
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

    public Uni<Void> publish(CashbackEvent event) {
        return eventBus.<Void>request("user.cashback", event)
                .replaceWithVoid();
    }
}