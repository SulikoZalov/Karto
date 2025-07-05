package org.project.karto.domain.card.value_objects;

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
        if (userID == null) throw new IllegalArgumentException("User id cannot be null");
        if (decaySpent == null) throw new IllegalArgumentException("decaySpent must not be null");
        if (lastTransactionDate == null) throw new IllegalArgumentException("lastTransactionDate must not be null");
        if (decaySpent.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("decaySpent must be non-negative");
        if (decayGiftCardsBought < 0) throw new IllegalArgumentException("decayGiftCardsBought must be non-negative");
        if (consecutiveActiveDays < 0) throw new IllegalArgumentException("consecutiveActiveDays must be non-negative");

        if (consecutiveActiveDays > DECAY.getDays()) consecutiveActiveDays = DECAY.getDays();
    }

    public static UserActivitySnapshot defaultSnapshot(UUID userID) {
        return new UserActivitySnapshot(userID, BigDecimal.ZERO, 0, LocalDateTime.now(), 0, false);
    }
}