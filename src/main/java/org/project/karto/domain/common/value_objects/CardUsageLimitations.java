package org.project.karto.domain.common.value_objects;

import java.time.Period;

public record CardUsageLimitations(Period expirationPeriod, int maxUsageCount) {
    public static final Period MIN_EXP_PERIOD = Period.ofMonths(1);
    public static final Period MAX_EXP_PERIOD = Period.ofMonths(3);

    public CardUsageLimitations {
        if (expirationPeriod == null || expirationPeriod.isZero() || expirationPeriod.isNegative())
            throw new IllegalArgumentException("Expiration period must be a positive non-zero value.");

        if (expirationPeriod.minus(MIN_EXP_PERIOD).isNegative() ||
                !expirationPeriod.minus(MAX_EXP_PERIOD).isNegative())
            throw new IllegalArgumentException("Expiration period must be between 1 and 3 months.");

        if (maxUsageCount < 1 || maxUsageCount > 10)
            throw new IllegalArgumentException("Usage count must be between 1 and 10.");
    }
}
