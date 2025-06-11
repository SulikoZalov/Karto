package org.project.karto.util;

import net.datafaker.Faker;
import org.project.karto.application.dto.auth.CompanyRegistrationForm;
import org.project.karto.application.dto.auth.RegistrationForm;
import org.project.karto.domain.card.entities.GiftCard;
import org.project.karto.domain.card.value_objects.*;
import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.common.value_objects.CardUsageLimitations;
import org.project.karto.domain.common.value_objects.Email;
import org.project.karto.domain.common.value_objects.Phone;
import org.project.karto.domain.companies.entities.Company;
import org.project.karto.domain.companies.value_objects.CompanyName;
import org.project.karto.domain.companies.value_objects.RegistrationNumber;
import org.project.karto.domain.user.values_objects.*;
import org.project.karto.infrastructure.security.HOTPGenerator;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TestDataGenerator {

    private static final Faker faker = new Faker();

    public static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    public static Company generateCompany() {
        return Company.of(
                generateRegistrationNumber(),
                generateCompanyName(),
                generateEmail(),
                generatePhone(),
                generatePassword(),
                HOTPGenerator.generateSecretKey(),
                generateCardLimits()
        );
    }

    public static CardUsageLimitations generateCardLimits() {
        int period = generateCardExpirationDays();
        int usages = generateCardUsageLimits();

        return CardUsageLimitations.of(period, usages);
    }

    public static CompanyName generateCompanyName() {
        while (true) {
            var companyName = Result.ofThrowable(() -> new CompanyName(faker.company().name()));
            if (!companyName.success()) continue;
            return companyName.value();
        }
    }
    
    public static String generateRegistrationCountryCode() {
        var codes = Locale.getISOCountries();
        return codes[faker.random().nextInt(codes.length)];
    }

    public static String generateRegistrationNumberValue() {
        var min = 5;
        var max = 20;

        var len = faker.random().nextInt(min, max);
        var buff = new StringBuilder(len);

        for (int i = 0; i < len; i++) {
            var option = faker.random().nextInt(1, 3);

            switch (option) {
                case 1 -> buff.append(faker.text().uppercaseCharacter());
                case 2 -> buff.append(faker.number().numberBetween(0, 9));
                case 3 -> buff.append('-');
            }
        }

        if (buff.length() < max) {
            buff.repeat('-', max - buff.length());
        }

        return buff.toString();
    }
    
    public static RegistrationNumber generateRegistrationNumber() {
        return new RegistrationNumber(
                generateRegistrationCountryCode(),
                generateRegistrationNumberValue()
        );
    }

    public static RegistrationForm generateRegistrationForm() {
        String password = generatePassword().password();
        return new RegistrationForm(
                generateFirstname().firstname(),
                generateSurname().surname(),
                generatePhone().phoneNumber(),
                generateEmail().email(),
                password,
                password,
                generateBirthdate().birthDate()
        );
    }

    public static int generateCardExpirationDays() {
        return faker.random().nextInt(30, 92);
    }

    public static int generateCardUsageLimits() {
        return faker.random().nextInt(1, 10);
    }

    public static CompanyRegistrationForm generateCompanyRegistrationForm() {
        return new CompanyRegistrationForm(
                generateRegistrationCountryCode(),
                generateRegistrationNumberValue(),
                generateCompanyName().companyName(),
                generateEmail().email(),
                generatePhone().phoneNumber(),
                generatePassword().password(),
                generateCardExpirationDays(),
                generateCardUsageLimits()
        );
    }

    public static Firstname generateFirstname() {
        while (true) {
            var firstnameResult = Result.ofThrowable(() -> new Firstname(faker.name().firstName()));
            if (!firstnameResult.success()) continue;
            return firstnameResult.value();
        }
    }

    public static Surname generateSurname() {
        while (true) {
            var surnameResult = Result.ofThrowable(() -> new Surname(faker.name().lastName()));
            if (!surnameResult.success()) continue;
            return surnameResult.value();
        }
    }

    public static Phone generatePhone() {
        while (true) {
            var phoneResult = Result.ofThrowable(() -> new Phone(faker.phoneNumber().phoneNumber()));
            if (!phoneResult.success()) continue;
            return phoneResult.value();
        }
    }

    public static Email generateEmail() {
        while (true) {
            var emailResult = Result.ofThrowable(() -> new Email(faker.internet().emailAddress()));
            if (!emailResult.success()) continue;
            return emailResult.value();
        }
    }

    public static Password generatePassword() {
        while (true) {
            var passwordResult = Result.ofThrowable(() -> new Password(faker.internet().password()));
            if (!passwordResult.success()) continue;
            return passwordResult.value();
        }
    }

    public static Birthdate generateBirthdate() {
        while (true) {
            var birthdateResult = Result.ofThrowable(() -> new Birthdate(faker.timeAndDate().birthday(18, 120)));
            if (!birthdateResult.success()) continue;
            return birthdateResult.value();
        }
    }

    public static BuyerID generateBuyerID() {
        return new BuyerID(UUID.randomUUID());
    }

    public static OwnerID generateOwnerID() {
        return new OwnerID(UUID.randomUUID());
    }

    public static Balance generateBalance() {
        return new Balance(BigDecimal.valueOf(faker.number().numberBetween(1000, 10000)));
    }

    public static Amount generateAmount(BigDecimal max) {
        return new Amount(BigDecimal.valueOf(faker.number().randomDouble(2, 1, max.intValue())));
    }

    public static String generateSecretKey() {
        return HOTPGenerator.generateSecretKey();
    }

    public static GiftCard generateSelfBougthGiftCard() {
        return GiftCard.selfBoughtCard(
                generatePAN(),
                new BuyerID(UUID.randomUUID()),
                generateBalance(),
                new StoreID(UUID.randomUUID()),
                generateSecretKey(),
                generateCardLimits()
        );
    }

    public static GiftCard generateSelfBougthGiftCard(Balance balance) {
        return GiftCard.selfBoughtCard(
                generatePAN(),
                new BuyerID(UUID.randomUUID()),
                balance,
                new StoreID(UUID.randomUUID()),
                generateSecretKey(),
                generateCardLimits()
        );
    }

    public static GiftCard generateBoughtAsGiftCard() {
        return GiftCard.boughtAsAGift(
                generatePAN(),
                new BuyerID(UUID.randomUUID()),
                generateBalance(),
                new OwnerID(UUID.randomUUID()),
                new StoreID(UUID.randomUUID()),
                generateSecretKey(),
                generateCardLimits()
        );
    }

    public static GiftCard generateBoughtAsGiftCard(Balance balance) {
        return GiftCard.boughtAsAGift(
                generatePAN(),
                new BuyerID(UUID.randomUUID()),
                balance,
                new OwnerID(UUID.randomUUID()),
                new StoreID(UUID.randomUUID()),
                generateSecretKey(),
                generateCardLimits()
        );
    }

    public static PAN generatePAN() {
        return new PAN(generateRandomCreditCardNumber());
    }

    public static String generateRandomCreditCardNumber() {
        String bin = "";
        int length = 16;
        int randomNumberLength = length - 1;

        StringBuilder builder = new StringBuilder(bin);
        for (int i = 0; i < randomNumberLength; i++) {
            int digit = RANDOM.nextInt(10);
            builder.append(digit);
        }

        int checkDigit = getCheckDigit(builder.toString());
        builder.append(checkDigit);

        return builder.toString();
    }

    private static int getCheckDigit(String number) {
        int sum = 0;
        for (int i = 0; i < number.length(); i++) {

            int digit = Integer.parseInt(number.substring(i, (i + 1)));

            if ((i % 2) == 0) {
                digit = digit * 2;
                if (digit > 9) {
                    digit = (digit / 10) + (digit % 10);
                }
            }
            sum += digit;
        }

        int mod = sum % 10;
        return ((mod == 0) ? 0 : 10 - mod);
    }
}
