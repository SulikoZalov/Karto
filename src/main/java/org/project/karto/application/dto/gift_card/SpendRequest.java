package org.project.karto.application.dto.gift_card;

import java.math.BigDecimal;
import java.util.UUID;

public record SpendRequest(
    UUID storeID,
    UUID fromCard,
    BigDecimal amount,
    String language) {
}
