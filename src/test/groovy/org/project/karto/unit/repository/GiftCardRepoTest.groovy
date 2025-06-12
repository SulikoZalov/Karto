package org.project.karto.unit.repository

import com.aingrace.test.spock.QuarkusSpockTest
import io.quarkus.test.common.QuarkusTestResource
import jakarta.enterprise.context.Dependent
import jakarta.inject.Inject
import org.project.karto.infrastructure.repository.JDBCGiftCardRepository
import org.project.karto.util.PostgresTestResource
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification

@Dependent
@QuarkusSpockTest
@QuarkusTestResource(value = PostgresTestResource.class)
class GiftCardRepoTest extends Specification {

    @Inject
    JDBCGiftCardRepository repo

    def "save gift card: self bougth"() {
        when:
        def result = repo.save(giftCard)

        then:
        result

        where:
        giftCard << (1..10).collect({TestDataGenerator.generateSelfBougthGiftCard()})
    }
}