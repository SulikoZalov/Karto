package org.project.karto.unit.qr

import org.project.karto.application.dto.common.QR
import org.project.karto.application.dto.gift_card.PaymentQRDTO
import org.project.karto.domain.common.containers.Result
import org.project.karto.infrastructure.qr.QRGenerator
import org.project.karto.util.TestDataGenerator
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

@Ignore
class QRGeneratorTest extends Specification {

    @Unroll
    def "should generate QR code successfully and return base64 string [#index]"() {
        given:
        PaymentQRDTO dto = new PaymentQRDTO(
                TestDataGenerator.generateCompanyName().companyName(),
                TestDataGenerator.generateAmount(BigDecimal.valueOf(9999999L)).value())

        when:
        Result<QR, Throwable> result = QRGenerator.generate(dto)

        then:
        result.success()
        result.value().value() != null

        where:
        index << (1..10_000)
    }
}


