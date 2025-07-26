package org.project.karto.application.dto.gift_card;

import java.util.UUID;

public record CardDTO(UUID partnerID, int expirationPeriodInDays, int maxUsageCount) {
}
