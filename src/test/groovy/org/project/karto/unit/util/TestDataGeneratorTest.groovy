package org.project.karto.unit.util

import net.datafaker.Faker
import org.project.karto.util.TestDataGenerator
import spock.lang.Shared
import spock.lang.Specification

class TestDataGeneratorTest extends Specification{

    @Shared faker = new Faker()

    void "generate companies"() {
        given:
        def max = 10000

        when:
        def companies = (1..max).collect({ TestDataGenerator.generateCompany()})

        then:
        notThrown(Exception)
        companies.every {it != null}
    }

    void "generate card limits"() {
        given:
        def max = 10000

        when:
        def limits = (1..max).collect({TestDataGenerator.generateCardLimits()})

        then:
        notThrown(Exception)
        limits.every {it != null}
    }

    void "generate company names"() {
        given:
        def max = 10000

        when:
        def names = (1..max).collect({TestDataGenerator.generateCompanyName()})

        then:
        notThrown(Exception)
        names.every {it != null}
    }

    void "generate registration numbers"() {
        given:
        def max = 10000

        when:
        def numbers = (1..max).collect({TestDataGenerator.generateRegistrationNumber()})

        then:
        notThrown(Exception)
        numbers.every {it != null}
    }

    void "generate registration forms"() {
        given:
        def max = 10000

        when:
        def forms = (1..max).collect({TestDataGenerator.generateRegistrationForm()})

        then:
        notThrown(Exception)
        forms.every {it != null}
    }

    void "generate first names"() {
        given:
        def max = 10000

        when:
        def firstNames = (1..max).collect({TestDataGenerator.generateFirstname()})

        then:
        notThrown(Exception)
        firstNames.every {it != null}
    }

    void "generate surnames"() {
        given:
        def max = 10000

        when:
        def surnames = (1..max).collect({TestDataGenerator.generateSurname()})

        then:
        notThrown(Exception)
        surnames.every {it != null}
    }

    void "generate phones"() {
        given:
        def max = 10000

        when:
        def phones = (1..max).collect({TestDataGenerator.generatePhone()})

        then:
        notThrown(Exception)
        phones.every {it != null}
    }

    void "generate emails"() {
        given:
        def max = 10000

        when:
        def emails = (1..max).collect({TestDataGenerator.generateEmail()})

        then:
        notThrown(Exception)
        emails.every {it != null}
    }

    void "generate passwords"() {
        given:
        def max = 10000

        when:
        def passwords = (1..max).collect({TestDataGenerator.generatePassword()})

        then:
        notThrown(Exception)
        passwords.every {it != null}
    }

    void "generate birth dates"() {
        given:
        def max = 10000

        when:
        def birthdates = (1..max).collect({TestDataGenerator.generateBirthdate()})

        then:
        notThrown(Exception)
        birthdates.every {it != null}
    }

    void "generate buyer ids"() {
        given:
        def max = 10000

        when:
        def buyerIDS = (1..max).collect({TestDataGenerator.generateBuyerID()})

        then:
        notThrown(Exception)
        buyerIDS.every {it != null}
    }

    void "generate owner ids"() {
        given:
        def max = 10000

        when:
        def ownerIDS = (1..max).collect({TestDataGenerator.generateOwnerID()})

        then:
        notThrown(Exception)
        ownerIDS.every {it != null}
    }

    void "generate balances"() {
        given:
        def max = 10000

        when:
        def balances = (1..max).collect({TestDataGenerator.generateBalance()})

        then:
        notThrown(Exception)
        balances.every {it != null}
    }

    void "generate amounts"() {
        given:
        def max = 10000
        def rand = BigDecimal.valueOf(faker.number().randomNumber())

        when:
        def amounts = (1..max).collect({TestDataGenerator.generateAmount(rand)})

        then:
        notThrown(Exception)
        amounts.every {it != null}
    }

    void "generate secret keys"() {
        given:
        def max = 10000

        when:
        def keys = (1..max).collect({TestDataGenerator.generateSecretKey()})

        then:
        notThrown(Exception)
        keys.every {it != null}
    }
}
