package org.project.karto.domain.companies.entities;

import org.project.karto.domain.common.value_objects.CardUsageLimitations;
import org.project.karto.domain.common.value_objects.Email;
import org.project.karto.domain.common.value_objects.KeyAndCounter;
import org.project.karto.domain.common.value_objects.Phone;
import org.project.karto.domain.companies.enumerations.CompanyStatus;
import org.project.karto.domain.companies.value_objects.RegistrationNumber;
import org.project.karto.domain.companies.value_objects.CompanyName;
import org.project.karto.domain.user.values_objects.Password;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Company {
    private final UUID id;
    private final RegistrationNumber registrationNumber;
    private final CompanyName companyName;
    private final Email email;
    private final Phone phone;
    private final LocalDateTime creationDate;
    private LocalDateTime lastUpdated;
    private Password password;
    private KeyAndCounter keyAndCounter;
    private CompanyStatus companyStatus;
    private CardUsageLimitations cardUsageLimitation;

    private Company(
            UUID id,
            RegistrationNumber registrationNumber,
            CompanyName companyName,
            Email email,
            Phone phone,
            LocalDateTime creationDate,
            LocalDateTime lastUpdated,
            Password password,
            KeyAndCounter keyAndCounter,
            CompanyStatus companyStatus,
            CardUsageLimitations cardUsageLimitation) {

        if (id == null) throw new IllegalArgumentException("id must not be null");
        if (registrationNumber == null) throw new IllegalArgumentException("registrationNumber must not be null");
        if (companyName == null) throw new IllegalArgumentException("companyName must not be null");
        if (email == null) throw new IllegalArgumentException("email must not be null");
        if (phone == null) throw new IllegalArgumentException("phone must not be null");
        if (creationDate == null) throw new IllegalArgumentException("creationDate must not be null");
        if (lastUpdated == null) throw new IllegalArgumentException("lastUpdate must not be null");
        if (password == null) throw new IllegalArgumentException("password must not be null");
        if (keyAndCounter == null) throw new IllegalArgumentException("keyAndCounter must not be null");
        if (companyStatus == null) throw new IllegalArgumentException("companyStatus must not be null");
        if (cardUsageLimitation == null) throw new IllegalArgumentException("cardUsagesLimitation must not be null");

        this.id = id;
        this.registrationNumber = registrationNumber;
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
        this.creationDate = creationDate;
        this.lastUpdated = lastUpdated;
        this.password = password;
        this.keyAndCounter = keyAndCounter;
        this.companyStatus = companyStatus;
        this.cardUsageLimitation = cardUsageLimitation;
    }

    public static Company of(
            RegistrationNumber registrationNumber,
            CompanyName companyName,
            Email email,
            Phone phone,
            Password password,
            String key,
            CardUsageLimitations cardUsagesLimitation) {

        LocalDateTime now = LocalDateTime.now();
        return new Company(UUID.randomUUID(), registrationNumber, companyName, email, phone,
                now, now, password, new KeyAndCounter(key, 0), CompanyStatus.PENDING, cardUsagesLimitation);
    }

    public static Company fromRepository(
            UUID id,
            RegistrationNumber registrationNumber,
            CompanyName companyName,
            Email email,
            Phone phone,
            LocalDateTime creationDate,
            LocalDateTime lastUpdate,
            Password password,
            KeyAndCounter keyAndCounter,
            CompanyStatus companyStatus,
            CardUsageLimitations cardUsagesLimitation) {

        return new Company(id, registrationNumber, companyName, email,
                phone, creationDate, lastUpdate, password, keyAndCounter, companyStatus, cardUsagesLimitation);
    }

    public UUID id() {
        return id;
    }

    public RegistrationNumber registrationNumber() {
        return registrationNumber;
    }

    public CompanyName companyName() {
        return companyName;
    }

    public Email email() {
        return email;
    }

    public Phone phone() {
        return phone;
    }

    public LocalDateTime creationDate() {
        return creationDate;
    }

    public LocalDateTime lastUpdated() {
        return lastUpdated;
    }

    public Password password() {
        return password;
    }

    public KeyAndCounter keyAndCounter() {
        return keyAndCounter;
    }

    public CompanyStatus companyStatus() {
        return companyStatus;
    }

    public boolean isActive() {
        return companyStatus == CompanyStatus.ACTIVE;
    }

    public void enable() {
        if (isActive())
            throw new IllegalStateException("You can`t active already verified user.");
        if (keyAndCounter.counter() == 0)
            throw new IllegalStateException("It is prohibited to activate an account that has not been verified.");

        this.companyStatus = CompanyStatus.ACTIVE;
        touch();
    }

    public void incrementCounter() {
        this.keyAndCounter = new KeyAndCounter(this.keyAndCounter.key(), this.keyAndCounter.counter() + 1);
        touch();
    }

    public void changePassword(Password password) {
        if (password == null)
            throw new IllegalArgumentException("Password can`t be null");
        if (companyStatus != CompanyStatus.ACTIVE)
            throw new IllegalArgumentException("Company account is not verified");

        this.password = password;
        touch();
    }

    public CardUsageLimitations cardUsageLimitation() {
        return cardUsageLimitation;
    }

    public void specifyCardUsageLimitations(CardUsageLimitations cardUsageLimitations) {
        if (cardUsageLimitations == null)
            throw new IllegalArgumentException("Card usage limitations can`t be null");
        if (companyStatus != CompanyStatus.ACTIVE)
            throw new IllegalArgumentException("Company account is not verified");

        this.cardUsageLimitation = cardUsageLimitations;
        touch();
    }

    private void touch() {
        this.lastUpdated = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        return Objects.equals(id, company.id) && Objects.equals(registrationNumber, company.registrationNumber) &&
                Objects.equals(companyName, company.companyName) && Objects.equals(email, company.email) &&
                Objects.equals(phone, company.phone) && Objects.equals(creationDate, company.creationDate) &&
                Objects.equals(password, company.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, registrationNumber, companyName, email, phone, creationDate, password);
    }
}
