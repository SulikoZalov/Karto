package org.project.karto.application.dto.gift_card;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.project.karto.domain.card.enumerations.GiftCardStatus;

public record GiftCardDTO(
    String giftCardID,
    String storeID,
    BigDecimal amount,
    GiftCardStatus status,
    int maxCountOfUsage,
    int remainingUsages,
    LocalDateTime expirationDate) {
}
