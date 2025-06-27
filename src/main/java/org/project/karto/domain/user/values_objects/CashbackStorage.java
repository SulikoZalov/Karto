package org.project.karto.domain.user.values_objects;

import java.math.BigDecimal;

public record CashbackStorage(BigDecimal amount) {
    public CashbackStorage {
        if (amount == null)
            throw new IllegalArgumentException("Amount can`t be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Amount can`t be bellow zero");
    }
}
