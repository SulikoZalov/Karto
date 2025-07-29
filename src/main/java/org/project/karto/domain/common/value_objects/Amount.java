package org.project.karto.domain.common.value_objects;

import org.project.karto.domain.card.value_objects.Balance;
import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

import java.math.BigDecimal;

public record Amount(BigDecimal value) {
    public Amount {
        if (value == null) throw new IllegalDomainArgumentException("Amount can`t be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) throw new IllegalDomainArgumentException("Amount can`t be bellow zero");
    }

    public Balance toBalance() {
        return new Balance(value);
    }
}
