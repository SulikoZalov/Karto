package org.project.karto.application.dto.gift_card;

import java.math.BigDecimal;

public record PaymentQRDTO(String partnerName, BigDecimal amount) {
}
