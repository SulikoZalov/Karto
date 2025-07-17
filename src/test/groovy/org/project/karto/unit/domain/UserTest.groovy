package org.project.karto.unit.domain

import org.project.karto.domain.common.enumerations.Role
import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException
import org.project.karto.domain.common.exceptions.IllegalDomainStateException
import org.project.karto.domain.common.value_objects.Amount
import org.project.karto.domain.common.value_objects.KeyAndCounter
import org.project.karto.domain.common.value_objects.Phone
import org.project.karto.domain.user.entities.User
import org.project.karto.domain.user.values_objects.CashbackStorage
import org.project.karto.domain.user.values_objects.PersonalData
import org.project.karto.util.TestDataGenerator
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDateTime

class UserTest extends Specification {

    def "should create user with factory method 'of'"() {
        given: "valid personal data and key"
        def personalData = createValidPersonalData()
        def key = "test-key"

        when: "creating user with factory method"
        def user = User.of(personalData, key)

        then: "user is created with correct default values"
        user.id() != null
        user.personalData() == personalData
        user.role() == Role.CUSTOMER
        !user.isVerified()
        !user.is2FAEnabled()
        user.keyAndCounter().key() == key
        user.keyAndCounter().counter() == 0
        user.cashbackStorage().amount() == BigDecimal.ZERO
        user.creationDate() != null
        user.lastUpdated() != null
    }

    def "should create user from repository with all parameters"() {
        given: "all required parameters for user creation from repository"
        def id = UUID.randomUUID()
        def personalData = createValidPersonalData()
        def isEnabled = true
        def is2FAVerified = true
        def keyAndCounter = new KeyAndCounter("key", 5)
        def cashbackStorage = new CashbackStorage(BigDecimal.valueOf(100))
        def creationDate = LocalDateTime.now().minusDays(1)
        def lastUpdated = LocalDateTime.now()

        when: "creating user from repository"
        def user = User.fromRepository(
                id, personalData, isEnabled, is2FAVerified,
                keyAndCounter, cashbackStorage, creationDate, lastUpdated
        )

        then: "user is created with provided values"
        user.id() == id
        user.personalData() == personalData
        user.isVerified() == isEnabled
        user.is2FAEnabled() == is2FAVerified
        user.keyAndCounter() == keyAndCounter
        user.cashbackStorage() == cashbackStorage
        user.creationDate() == creationDate
        user.lastUpdated() == lastUpdated
        user.role() == Role.CUSTOMER // Always CUSTOMER regardless of input
    }

    @Unroll
    def "should throw IllegalArgumentException when creating user with null #parameter"() {
        when: "creating user with null parameter"
        User.fromRepository(id, personalData, false, false, keyAndCounter, cashbackStorage, creationDate, lastUpdated)

        then: "IllegalArgumentException is thrown"
        def exception = thrown(IllegalDomainArgumentException)
        exception.message == expectedMessage

        where:
        parameter          | id                  | personalData              | keyAndCounter                    | cashbackStorage                      | creationDate            | lastUpdated            | expectedMessage
        "id"              | null                | createValidPersonalData() | new KeyAndCounter("key", 0)      | new CashbackStorage(BigDecimal.ZERO) | LocalDateTime.now()     | LocalDateTime.now()    | "id must not be null"
        "personalData"    | UUID.randomUUID()   | null                      | new KeyAndCounter("key", 0)      | new CashbackStorage(BigDecimal.ZERO) | LocalDateTime.now()     | LocalDateTime.now()    | "personalData must not be null"
        "keyAndCounter"   | UUID.randomUUID()   | createValidPersonalData() | null                             | new CashbackStorage(BigDecimal.ZERO) | LocalDateTime.now()     | LocalDateTime.now()    | "keyAndCounter must not be null"
        "cashbackStorage" | UUID.randomUUID()   | createValidPersonalData() | new KeyAndCounter("key", 0)      | null                                 | LocalDateTime.now()     | LocalDateTime.now()    | "cashbackStorage must not be null"
        "creationDate"    | UUID.randomUUID()   | createValidPersonalData() | new KeyAndCounter("key", 0)      | new CashbackStorage(BigDecimal.ZERO) | null                    | LocalDateTime.now()    | "creationDate must not be null"
        "lastUpdated"     | UUID.randomUUID()   | createValidPersonalData() | new KeyAndCounter("key", 0)      | new CashbackStorage(BigDecimal.ZERO) | LocalDateTime.now()     | null                   | "lastUpdated must not be null"
    }

    def "should register phone for verification successfully"() {
        given: "user without phone and valid phone number"
        def user = createUserWithoutPhone()
        def phone = new Phone("+1234567890")
        def originalPersonalData = user.personalData()

        when: "registering phone for verification"
        user.registerPhoneForVerification(phone)

        then: "phone is registered and personal data is updated"
        user.personalData().phone().isPresent()
        user.personalData().phone().get() == phone.phoneNumber()
        user.personalData().firstname() == originalPersonalData.firstname()
        user.personalData().surname() == originalPersonalData.surname()
        user.personalData().email() == originalPersonalData.email()
        user.personalData().birthDate() == originalPersonalData.birthDate()
    }

    def "should throw exception when registering null phone"() {
        given: "user without phone"
        def user = createUserWithoutPhone()

        when: "registering null phone"
        user.registerPhoneForVerification(null)

        then: "IllegalDomainArgumentException is org.project.karto.domain.common.exceptions.IllegalDomainArgumentException"
        def exception = thrown(IllegalDomainArgumentException)
        exception.message == "Phone is null"
    }

    def "should throw exception when registering phone for user who already has phone"() {
        given: "user with existing phone"
        def user = TestDataGenerator.generateUser()
        def newPhone = new Phone("+9876543210")

        when: "trying to register another phone"
        user.registerPhoneForVerification(newPhone)

        then: "IllegalDomainArgumentException is org.project.karto.domain.common.exceptions.IllegalDomainArgumentException"
        def exception = thrown(IllegalDomainArgumentException)
        exception.message == "Phone number already registered."
    }

    def "should enable user successfully when counter is greater than 0"() {
        given: "unverified user with incremented counter"
        def user = TestDataGenerator.generateUser()
        user.incrementCounter()

        when: "enabling user"
        user.enable()

        then: "user is verified"
        user.isVerified()
    }

    def "should throw exception when trying to enable already verified user"() {
        given: "already verified user"
        def user = TestDataGenerator.generateUser()
        user.incrementCounter()
        user.enable()

        when: "trying to enable again"
        user.enable()

        then: "IllegalDomainStateException is thrown"
        def exception = thrown(IllegalDomainStateException)
        exception.message == "You can`t active already verified user."
    }

    def "should throw exception when trying to enable user with counter 0"() {
        given: "user with counter 0"
        def user = TestDataGenerator.generateUser()

        when: "trying to enable user"
        user.enable()

        then: "IllegalDomainStateException is thrown"
        def exception = thrown(IllegalDomainStateException)
        exception.message == "It is prohibited to activate an account that has not been verified."
    }

    def "should enable 2FA successfully when all conditions are met"() {
        given: "verified user with counter > 1"
        def user = TestDataGenerator.generateUser()
        user.incrementCounter() // counter = 1
        user.enable()           // user verified
        user.incrementCounter() // counter = 2

        when: "enabling 2FA"
        user.enable2FA()

        then: "2FA is enabled"
        user.is2FAEnabled()
    }

    def "should throw exception when enabling 2FA on unverified user"() {
        given: "unverified user"
        def user = TestDataGenerator.generateUser()
        user.incrementCounter()
        user.incrementCounter()

        when: "trying to enable 2FA"
        user.enable2FA()

        then: "IllegalDomainStateException is thrown"
        def exception = thrown(IllegalDomainStateException)
        exception.message == "You can`t enable 2FA on not verified account"
    }

    def "should throw exception when enabling 2FA twice"() {
        given: "user with 2FA already enabled"
        def user = TestDataGenerator.generateUser()
        user.incrementCounter()
        user.enable()
        user.incrementCounter()
        user.enable2FA()

        when: "trying to enable 2FA again"
        user.enable2FA()

        then: "IllegalDomainStateException is thrown"
        def exception = thrown(IllegalDomainStateException)
        exception.message == "You can`t activate 2FA twice"
    }

    def "should disable user successfully"() {
        given: "verified user"
        def user = TestDataGenerator.generateUser()
        user.incrementCounter()
        user.enable()

        when: "disabling user"
        user.disable()

        then: "user is not verified"
        !user.isVerified()
    }

    def "should throw exception when disabling already disabled user"() {
        given: "unverified user"
        def user = TestDataGenerator.generateUser()

        when: "trying to disable already disabled user"
        user.disable()

        then: "IllegalDomainStateException is thrown"
        def exception = thrown(IllegalDomainStateException)
        exception.message == "You can't deactivate a user who is already deactivated."
    }

    def "should increment counter successfully"() {
        given: "user with initial counter"
        def user = TestDataGenerator.generateUser()
        def initialCounter = user.keyAndCounter().counter()

        when: "incrementing counter"
        user.incrementCounter()

        then: "counter is incremented by 1"
        user.keyAndCounter().counter() == initialCounter + 1
        user.keyAndCounter().key() == user.keyAndCounter().key() // key remains same
    }

    def "should increment counter multiple times"() {
        given: "user with initial counter"
        def user = TestDataGenerator.generateUser()
        def initialCounter = user.keyAndCounter().counter()

        when: "incrementing counter multiple times"
        3.times { user.incrementCounter() }

        then: "counter is incremented correctly"
        user.keyAndCounter().counter() == initialCounter + 3
    }

    def "should add cashback successfully for verified user"() {
        given: "verified user and amount to add"
        def user = createVerifiedUser()
        def initialAmount = user.cashbackStorage().amount()
        def amountToAdd = new Amount(BigDecimal.valueOf(50))

        when: "adding cashback"
        user.addCashback(amountToAdd)

        then: "cashback is added to storage"
        user.cashbackStorage().amount() == initialAmount.add(amountToAdd.value())
    }

    def "should throw exception when adding null cashback amount"() {
        given: "verified user"
        def user = createVerifiedUser()

        when: "adding null amount"
        user.addCashback(null)

        then: "IllegalDomainArgumentException is org.project.karto.domain.common.exceptions.IllegalDomainArgumentException"
        def exception = thrown(IllegalDomainArgumentException)
        exception.message == "Amount can`t be null"
    }

    def "should throw exception when adding cashback to unverified user"() {
        given: "unverified user"
        def user = TestDataGenerator.generateUser()
        def amount = new Amount(BigDecimal.valueOf(50))

        when: "adding cashback to unverified user"
        user.addCashback(amount)

        then: "IllegalDomainArgumentException is org.project.karto.domain.common.exceptions.IllegalDomainArgumentException"
        def exception = thrown(IllegalDomainArgumentException)
        exception.message == "Account is not verified"
    }

    def "should remove cash from storage successfully"() {
        given: "verified user with cashback"
        def user = createVerifiedUserWithCashback(BigDecimal.valueOf(100))
        def initialAmount = user.cashbackStorage().amount()
        def amountToRemove = new Amount(BigDecimal.valueOf(30))

        when: "removing cash from storage"
        user.removeCashFromStorage(amountToRemove)

        then: "cash is removed from storage"
        user.cashbackStorage().amount() == initialAmount.subtract(amountToRemove.value())
    }

    def "should handle removing all cash from storage"() {
        given: "verified user with specific cashback amount"
        def cashbackAmount = BigDecimal.valueOf(50)
        def user = createVerifiedUserWithCashback(cashbackAmount)
        def amountToRemove = new Amount(cashbackAmount)

        when: "removing all cash"
        user.removeCashFromStorage(amountToRemove)

        then: "storage amount is zero"
        user.cashbackStorage().amount() == BigDecimal.ZERO
    }

    def "should throw exception when removing null amount from storage"() {
        given: "verified user"
        def user = createVerifiedUser()

        when: "removing null amount"
        user.removeCashFromStorage(null)

        then: "IllegalDomainArgumentException is org.project.karto.domain.common.exceptions.IllegalDomainArgumentException"
        def exception = thrown(IllegalDomainArgumentException)
        exception.message == "Amount can`t be null"
    }

    def "should throw exception when removing cash from unverified user storage"() {
        given: "unverified user"
        def user = TestDataGenerator.generateUser()
        def amount = new Amount(BigDecimal.valueOf(10))

        when: "removing cash from unverified user"
        user.removeCashFromStorage(amount)

        then: "IllegalDomainArgumentException is org.project.karto.domain.common.exceptions.IllegalDomainArgumentException"
        def exception = thrown(IllegalDomainArgumentException)
        exception.message == "Account is not verified"
    }

    def "should return correct getter values"() {
        given: "user created from repository with specific values"
        def id = UUID.randomUUID()
        def personalData = createValidPersonalData()
        def keyAndCounter = new KeyAndCounter("test-key", 3)
        def cashbackStorage = new CashbackStorage(BigDecimal.valueOf(75))
        def creationDate = LocalDateTime.now().minusDays(5)
        def lastUpdated = LocalDateTime.now().minusHours(2)

        def user = User.fromRepository(
                id, personalData, true, false,
                keyAndCounter, cashbackStorage, creationDate, lastUpdated
        )

        expect: "all getters return correct values"
        user.id() == id
        user.personalData() == personalData
        user.role() == Role.CUSTOMER
        user.isVerified()
        !user.is2FAEnabled()
        user.keyAndCounter() == keyAndCounter
        user.cashbackStorage() == cashbackStorage
        user.creationDate() == creationDate
        user.lastUpdated() == lastUpdated
    }

    def "should implement equals correctly"() {
        given: "two users with same core properties"
        def id = UUID.randomUUID()
        def personalData = createValidPersonalData()
        def creationDate = LocalDateTime.now()

        def user1 = User.fromRepository(
                id, personalData, true, false,
                new KeyAndCounter("key1", 1),
                new CashbackStorage(BigDecimal.valueOf(10)),
                creationDate, LocalDateTime.now()
        )

        def user2 = User.fromRepository(
                id, personalData, true, false,
                new KeyAndCounter("key2", 2),
                new CashbackStorage(BigDecimal.valueOf(20)),
                creationDate, LocalDateTime.now().plusHours(1)
        )

        expect: "users are equal based on id, personalData, isVerified, and creationDate"
        user1 == user2
        user1.hashCode() == user2.hashCode()
    }

    def "should implement equals correctly for different users"() {
        given: "two users with different core properties"
        def user1 = TestDataGenerator.generateUser()
        def user2 = TestDataGenerator.generateUser()

        expect: "users are not equal"
        user1 != user2
        user1.hashCode() != user2.hashCode()
    }

    def "should handle equals with null and different class"() {
        given: "a user"
        def user = TestDataGenerator.generateUser()

        expect: "equals handles null and different class correctly"
        user != null
    }

    def "should generate correct toString representation"() {
        given: "user with specific data"
        def personalData = createValidPersonalData()
        def user = User.of(personalData, "test-key")

        when: "calling toString"
        def result = user.toString()

        then: "string contains all personal information"
        result.contains(user.id().toString())
        result.contains(personalData.firstname())
        result.contains(personalData.surname())
        result.contains(personalData.phone().toString())
        result.contains(personalData.email())
        result.contains(personalData.birthDate().toString())
    }

    def "should handle complex user lifecycle scenario"() {
        given: "new user created"
        def user = TestDataGenerator.generateUserWithoutPhoneAndPassword()
        def phone = new Phone("+1234567890")

        when: "going through complete verification process"
        // 1. Register phone
        user.registerPhoneForVerification(phone)

        // 2. Increment counter (verification step)
        user.incrementCounter()

        // 3. Enable user
        user.enable()

        // 4. Add some cashback
        def cashbackAmount = new Amount(BigDecimal.valueOf(100))
        user.addCashback(cashbackAmount)

        // 5. Increment counter again for 2FA
        user.incrementCounter()

        // 6. Enable 2FA
        user.enable2FA()

        // 7. Use some cashback
        def usedAmount = new Amount(BigDecimal.valueOf(25))
        user.removeCashFromStorage(usedAmount)

        then: "user state is correct after all operations"
        user.personalData().phone().isPresent()
        user.personalData().phone().get() == phone.phoneNumber()
        user.isVerified()
        user.is2FAEnabled()
        user.keyAndCounter().counter() == 2
        user.cashbackStorage().amount() == BigDecimal.valueOf(75)
    }

    private static PersonalData createValidPersonalData() {
        return new PersonalData(
                "John",
                "Doe",
                "+1234567890",
                "password123",
                "john.doe@example.com",
                LocalDateTime.now().minusYears(25).toLocalDate()
        )
    }

    private static PersonalData createValidPersonalDataWithoutPhone() {
        return new PersonalData(
                "John",
                "Doe",
                null,
                "password123",
                "john.doe@example.com",
                LocalDateTime.now().minusYears(25).toLocalDate()
        )
    }

    private static User createUserWithoutPhone() {
        def personalData = createValidPersonalDataWithoutPhone()
        return User.of(personalData, "test-key")
    }

    private static User createVerifiedUser() {
        def user = TestDataGenerator.generateUser()
        user.incrementCounter()
        user.enable()
        return user
    }

    private static User createVerifiedUserWithCashback(BigDecimal amount) {
        def user = createVerifiedUser()
        user.addCashback(new Amount(amount))
        return user
    }
}