package org.project.karto.domain.card.value_objects;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;
import org.project.karto.domain.common.exceptions.IllegalDomainStateException;
import org.project.karto.domain.common.value_objects.Amount;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Fee(BigDecimal rate) {

    public Fee {
        if (rate == null) throw new IllegalDomainArgumentException("Fee rate cannot be null");
        if (rate.compareTo(BigDecimal.ZERO) < 0) throw new IllegalDomainArgumentException("Fee rate cannot be negative");
    }

    public static Fee defaultFee() {
        return new Fee(BigDecimal.ZERO);
    }

    /**
     * Calculates the fee amount based on the given amount and the fee rate.
     *
     * <p>The fee is calculated as {@code amount * rate}, with the result rounded
     * up to 2 decimal places using {@link RoundingMode#UP}.</p>
     *
     * @param amount the base amount on which the fee is calculated (must not be null)
     * @return the calculated fee as an {@link Amount}
     * @throws IllegalDomainArgumentException if {@code amount} is null
     */
    public Amount calculateFee(Amount amount) {
        if (amount == null) throw new IllegalDomainArgumentException("Amount cannot be null");

        BigDecimal feeValue = amount.value()
                .multiply(rate)
                .setScale(2, RoundingMode.UP);

        return new Amount(feeValue);
    }

    /**
     * Calculates the gross amount required to receive the specified target net amount after fee deduction.
     *
     * <p>The calculation formula is: {@code gross = targetAmount / (1 - rate)},
     * with the result rounded up to 2 decimal places using {@link RoundingMode#UP}.</p>
     *
     * <p>If the fee rate is 100% or more, this method will throw an exception since the calculation becomes invalid (divisor ≤ 0).</p>
     *
     * @param targetAmount the desired net amount after fee deduction (must not be null)
     * @return the gross amount needed to achieve the target net amount
     * @throws IllegalDomainArgumentException if {@code targetAmount} is null
     * @throws IllegalDomainStateException if the fee rate is 100% or more (divisor ≤ 0)
     */
    public Amount grossAmountForNet(Amount targetAmount) {
        if (targetAmount == null) throw new IllegalDomainArgumentException("Target amount cannot be null");

        BigDecimal divisor = BigDecimal.ONE.subtract(rate);
        if (divisor.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalDomainStateException("Fee rate is too high for calculation (divisor <= 0)");

        BigDecimal gross = targetAmount.value()
                .divide(divisor, 2, RoundingMode.UP);

        return new Amount(gross);
    }
}
