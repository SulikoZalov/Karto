package org.project.karto.unit.security

import org.project.karto.infrastructure.security.HOTPGenerator
import spock.lang.Specification

class HOTPGeneratorTest extends Specification {

    def "generateSecretKey should return non-blank base64 string of 20 bytes"() {
        when:
        String key = HOTPGenerator.generateSecretKey()
        byte[] decoded = Base64.decoder.decode(key)

        then:
        key != null
        !key.blank
        decoded.length == 20
    }

    def "generateHOTP with default settings should return 6-digit number"() {
        given:
        def generator = new HOTPGenerator()
        def key = HOTPGenerator.generateSecretKey()

        when:
        def hotp = generator.generateHOTP(key, 1L)

        then:
        hotp != null
        hotp.length() == 6
        hotp ==~ /\d{6}/
    }

    def "generateHOTP should produce correct length for different password lengths"() {
        given:
        def key = HOTPGenerator.generateSecretKey()

        expect:
        new HOTPGenerator(HOTPGenerator.DEFAULT_ALGORITHM, 6).generateHOTP(key, 1L).length() == 6
        new HOTPGenerator(HOTPGenerator.DEFAULT_ALGORITHM, 7).generateHOTP(key, 1L).length() == 7
        new HOTPGenerator(HOTPGenerator.DEFAULT_ALGORITHM, 8).generateHOTP(key, 1L).length() == 8
    }

    def "generateHOTP should produce same result for same key and counter"() {
        given:
        def generator = new HOTPGenerator()
        def key = HOTPGenerator.generateSecretKey()
        def counter = 123456L

        expect:
        generator.generateHOTP(key, counter) == generator.generateHOTP(key, counter)
    }

    def "constructor should throw exception for invalid password length"() {
        when:
        new HOTPGenerator(HOTPGenerator.DEFAULT_ALGORITHM, 5)

        then:
        thrown(IllegalArgumentException)
    }

    def "constructor should throw exception for invalid algorithm"() {
        when:
        new HOTPGenerator("InvalidAlgo")

        then:
        thrown(IllegalArgumentException)
    }

    def "generateHOTP should throw exception for invalid base64 key"() {
        given:
        def generator = new HOTPGenerator()

        when:
        generator.generateHOTP("this_is_not_base64", 10L)

        then:
        thrown(IllegalArgumentException)
    }

    def "generateHOTP should work for very large counter"() {
        given:
        def generator = new HOTPGenerator()
        def key = HOTPGenerator.generateSecretKey()

        when:
        def hotp = generator.generateHOTP(key, Long.MAX_VALUE)

        then:
        hotp != null
        hotp.length() == 6
    }

    def "generateHOTP should produce different results for different counters"() {
        given:
        def generator = new HOTPGenerator()
        def key = HOTPGenerator.generateSecretKey()

        when:
        def code1 = generator.generateHOTP(key, 1L)
        def code2 = generator.generateHOTP(key, 2L)

        then:
        code1 != code2
    }
}
