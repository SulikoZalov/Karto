package org.project.karto.unit.repository

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.domain.card.entities.CardVerificationOTP
import org.project.karto.infrastructure.repository.JDBCCardVerificationOTPRepository
import org.project.karto.infrastructure.repository.JDBCGiftCardRepository
import org.project.karto.infrastructure.security.HOTPGenerator
import org.project.karto.util.PostgresTestResource
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification

@Dependent
@QuarkusSpockTest
@QuarkusTestResource(value = PostgresTestResource.class)
class CardVerificationOTPRepoTest extends Specification{

    @Inject
    JDBCCardVerificationOTPRepository verificationOTPRepository

    @Inject
    JDBCGiftCardRepository cardRepository;

    HOTPGenerator hotpGenerator

    def setup() {
        hotpGenerator = new HOTPGenerator()
    }

    void "successfully save card otp"() {
        given:
        def otp = CardVerificationOTP.of(card.id(),
                hotpGenerator.generateHOTP(card.keyAndCounter().key(), card.keyAndCounter().counter()))
        cardRepository.save(card)

        when:
        def saveResult = verificationOTPRepository.save(otp)

        then:
        notThrown(Exception)
        saveResult.success()

        where:
        card << (1..10).collect { TestDataGenerator.generateSelfBougthGiftCard()}
    }
}
