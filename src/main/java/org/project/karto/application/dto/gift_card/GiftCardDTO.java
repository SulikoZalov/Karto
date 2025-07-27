package org.project.karto.application.dto.gift_card;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.project.karto.domain.card.enumerations.GiftCardStatus;
import org.project.karto.domain.common.annotations.Nullable;

public record GiftCardDTO(
        String giftCardID,
        @Nullable String storeID,
        @Nullable String storeName,
        BigDecimal amount,
        GiftCardStatus status,
        int maxCountOfUsage,
        int countOfUses,
        LocalDateTime expirationDate) {
}
