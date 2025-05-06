package org.project.karto.domain.card.value_objects;

import java.math.BigDecimal;

public record Balance(BigDecimal value) {
    public Balance {
        if (value == null) throw new IllegalArgumentException("Value can`t be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Value can`t be negative");
    }
}
