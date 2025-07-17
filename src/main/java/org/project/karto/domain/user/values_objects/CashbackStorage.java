package org.project.karto.domain.user.values_objects;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

import java.math.BigDecimal;

public record CashbackStorage(BigDecimal amount) {
    public CashbackStorage {
        if (amount == null)
            throw new IllegalDomainArgumentException("Amount can`t be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalDomainArgumentException("Amount can`t be bellow zero");
    }
}
