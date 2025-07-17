package org.project.karto.domain.card.value_objects;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

import java.math.BigDecimal;

public record Balance(BigDecimal value) {
    public Balance {
        if (value == null) throw new IllegalDomainArgumentException("Value can`t be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) throw new IllegalDomainArgumentException("Value can`t be negative");
    }
}
