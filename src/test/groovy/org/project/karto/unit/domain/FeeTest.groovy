package org.project.karto.unit.domain

import org.project.karto.domain.card.value_objects.Fee
import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException
import org.project.karto.domain.common.exceptions.IllegalDomainStateException
import org.project.karto.domain.common.value_objects.Amount
import spock.lang.Specification
import spock.lang.Unroll

class FeeTest extends Specification {

    def "should create Fee with valid rate"() {
        expect:
        new Fee(BigDecimal.valueOf(0.1)).rate() == BigDecimal.valueOf(0.1)
    }

    def "should throw exception for null rate"() {
        when:
        new Fee(null)

        then:
        IllegalDomainArgumentException e = thrown()
        e.message == "Fee rate cannot be null"
    }

    def "should throw exception for negative rate"() {
        when:
        new Fee(BigDecimal.valueOf(-0.05))

        then:
        IllegalDomainArgumentException e = thrown()
        e.message == "Fee rate cannot be negative"
    }

    def "defaultFee should return Fee with zero rate"() {
        expect:
        Fee.defaultFee().rate() == BigDecimal.ZERO
    }

    @Unroll
    def "calculateFee should return correct fee for amount=#amount and rate=#rate"() {
        given:
        Fee fee = new Fee(rate)

        expect:
        fee.calculateFee(new Amount(amount)).value() == expectedFee

        where:
        amount    | rate          || expectedFee
        100.00    | 0.10          || 10.00
        200.00    | 0.075         || 15.00
        50.00     | 0.033         || 1.65
        123.45    | 0.05          || 6.18
    }

    def "calculateFee should throw exception if amount is null"() {
        given:
        Fee fee = new Fee(BigDecimal.valueOf(0.1))

        when:
        fee.calculateFee(null)

        then:
        IllegalDomainArgumentException e = thrown()
        e.message == "Amount cannot be null"
    }

    @Unroll
    def "grossAmountForNet should calculate correct gross for targetAmount=#targetAmount and rate=#rate"() {
        given:
        Fee fee = new Fee(rate)

        expect:
        fee.grossAmountForNet(new Amount(targetAmount)).value() == expectedGross

        where:
        targetAmount | rate          || expectedGross
        90.00        | 0.10          || 100.00
        100.00       | 0.075         || 108.11
        48.50        | 0.033         || 50.16
        117.00       | 0.05          || 123.16
    }

    def "grossAmountForNet should throw exception if targetAmount is null"() {
        given:
        Fee fee = new Fee(BigDecimal.valueOf(0.1))

        when:
        fee.grossAmountForNet(null)

        then:
        IllegalDomainArgumentException e = thrown()
        e.message == "Target amount cannot be null"
    }

    def "grossAmountForNet should throw exception if rate is 100% or more"() {
        given:
        Fee fee = new Fee(rate)

        when:
        fee.grossAmountForNet(new Amount(BigDecimal.valueOf(100)))

        then:
        IllegalDomainStateException e = thrown()
        e.message == "Fee rate is too high for calculation (divisor <= 0)"

        where:
        rate << [BigDecimal.ONE, new BigDecimal("1.2")]
    }
}
