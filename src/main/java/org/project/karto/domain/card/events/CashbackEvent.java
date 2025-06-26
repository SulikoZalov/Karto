package org.project.karto.domain.card.events;

import org.project.karto.domain.common.interfaces.KartoDomainEvent;

import java.math.BigDecimal;
import java.util.UUID;

public record CashbackEvent(UUID ownerID, BigDecimal amount) implements KartoDomainEvent {
    public CashbackEvent {
        if (ownerID == null)
            throw new IllegalArgumentException("OwnerID cannot ba null");
        if (amount == null)
            throw new IllegalArgumentException("Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Amount can`t be bellow zero");
    }
}
