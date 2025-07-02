package org.project.karto.domain.card.value_objects;

import java.math.BigDecimal;

public record ExternalFeeAmount(BigDecimal value) {

    public ExternalFeeAmount {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new  IllegalArgumentException("value must be greater than zero");
        }
    }
}
