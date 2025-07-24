package org.project.karto.infrastructure.consumer;

import io.quarkus.vertx.ConsumeEvent;
import org.project.karto.domain.card.events.CashbackEvent;
import org.project.karto.domain.common.value_objects.Amount;
import org.project.karto.domain.user.entities.User;
import org.project.karto.domain.user.repository.UserRepository;

public class EventConsumer {

    private final UserRepository userRepository;

    EventConsumer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ConsumeEvent("user.cashback")
    public void consume(CashbackEvent event) {
        User user = userRepository.findBy(event.ownerID().value())
                .orElseThrow();

        user.addCashback(new Amount(event.amount()), event.reachMaxCashbackRate());
        userRepository.updateCashbackStorage(user);
    }
}
