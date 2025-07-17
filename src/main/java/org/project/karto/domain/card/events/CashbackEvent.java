package org.project.karto.domain.card.events;

import org.project.karto.domain.card.value_objects.CardID;
import org.project.karto.domain.card.value_objects.OwnerID;
import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;
import org.project.karto.domain.common.interfaces.KartoDomainEvent;

import java.math.BigDecimal;

public record CashbackEvent(CardID cardID, OwnerID ownerID, BigDecimal amount) implements KartoDomainEvent {

    public CashbackEvent {
        if (cardID == null) throw new IllegalDomainArgumentException("Card id cannot be null");
        if (ownerID == null) throw new IllegalDomainArgumentException("User id cannot be null");
        if (cardID.value().equals(ownerID.value()))
            throw new IllegalDomainArgumentException("Do not match");

        if (amount == null) throw new IllegalDomainArgumentException("Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) throw new IllegalDomainArgumentException("Amount cannot be negative");
    }
}
