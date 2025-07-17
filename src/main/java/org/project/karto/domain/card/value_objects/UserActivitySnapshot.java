package org.project.karto.domain.card.value_objects;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.UUID;

public record UserActivitySnapshot(
        UUID userID,
        BigDecimal decaySpent,
        long decayGiftCardsBought,
        LocalDateTime lastTransactionDate,
        int consecutiveActiveDays,
        boolean lastUsageReachedMaximumCashbackRate) {

    public static final Period DECAY = Period.ofDays(14);

    public UserActivitySnapshot {
        if (userID == null) throw new IllegalDomainArgumentException("User id cannot be null");
        if (decaySpent == null) throw new IllegalDomainArgumentException("decaySpent must not be null");
        if (lastTransactionDate == null) throw new IllegalDomainArgumentException("lastTransactionDate must not be null");
        if (decaySpent.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalDomainArgumentException("decaySpent must be non-negative");
        if (decayGiftCardsBought < 0) throw new IllegalDomainArgumentException("decayGiftCardsBought must be non-negative");
        if (consecutiveActiveDays < 0) throw new IllegalDomainArgumentException("consecutiveActiveDays must be non-negative");

        if (consecutiveActiveDays > DECAY.getDays()) consecutiveActiveDays = DECAY.getDays();
    }

    public static UserActivitySnapshot defaultSnapshot(UUID userID) {
        return new UserActivitySnapshot(userID, BigDecimal.ZERO, 0, LocalDateTime.now(), 0, false);
    }
}