package org.project.karto.domain.card.value_objects;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserActivitySnapshot(
        UUID userID,
        BigDecimal totalAmountSpent,
        long totalGiftCardsBought,
        LocalDateTime lastTransactionDate,
        int consecutiveActiveDays,
        boolean lastUsageReachedMaximumCashbackRate) {

    public UserActivitySnapshot {
        if (userID == null) throw new IllegalArgumentException("User id cannot be null");
        if (totalAmountSpent == null) throw new IllegalArgumentException("totalAmountSpent must not be null");
        if (lastTransactionDate == null) throw new IllegalArgumentException("lastTransactionDate must not be null");
        if (totalAmountSpent.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("totalAmountSpent must be non-negative");
        if (totalGiftCardsBought < 0) throw new IllegalArgumentException("totalGiftCardsBought must be non-negative");
        if (consecutiveActiveDays < 0) throw new IllegalArgumentException("consecutiveActiveDays must be non-negative");
    }

    public static UserActivitySnapshot defaultSnapshot(UUID userID) {
        return new UserActivitySnapshot(userID, BigDecimal.ZERO, 0, LocalDateTime.now(), 0, false);
    }
}
