package org.project.karto.unit.qr

import org.project.karto.infrastructure.qr.QRGenerator
import spock.lang.Specification
import org.project.karto.application.dto.gift_card.PaymentQRDTO
import org.project.karto.application.dto.common.QR
import org.project.karto.domain.common.containers.Result
import org.project.karto.util.TestDataGenerator

class QRGeneratorTest extends Specification {

    def qrGenerator = new QRGenerator()

    def "should generate QR code successfully and return base64 string"() {
        given:
        def dto = new PaymentQRDTO(
        TestDataGenerator.generateCompanyName().companyName(),
        TestDataGenerator.generateAmount(BigDecimal.valueOf(9999999L)).value()
        )


        when:
        Result<QR, Throwable> result = qrGenerator.generate(dto)

        then:
        result.success()
        result.value().value() != null
        result.value().value().startsWith("iVBOR")
    }
}


