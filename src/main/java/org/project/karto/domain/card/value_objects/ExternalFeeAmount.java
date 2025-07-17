package org.project.karto.domain.card.value_objects;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

import java.math.BigDecimal;

public record ExternalFeeAmount(BigDecimal value) {

    public ExternalFeeAmount {
        if (value == null) {
            throw new IllegalDomainArgumentException("value must not be null");
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalDomainArgumentException("value must be greater than zero");
        }
    }
}
