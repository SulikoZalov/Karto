package org.project.karto.domain.card.value_objects;

import java.math.BigDecimal;

public record Amount(BigDecimal value) {
    public Amount {
        if (value == null) throw new IllegalArgumentException("Amount can`t be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Amount can`t be bellow zero");
    }
}
