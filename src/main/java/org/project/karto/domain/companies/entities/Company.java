package org.project.karto.domain.companies.entities;

import org.project.karto.domain.common.annotations.Nullable;
import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;
import org.project.karto.domain.common.exceptions.IllegalDomainStateException;
import org.project.karto.domain.common.value_objects.*;
import org.project.karto.domain.companies.enumerations.CompanyStatus;
import org.project.karto.domain.companies.value_objects.CompanyName;
import org.project.karto.domain.companies.value_objects.RegistrationNumber;
import org.project.karto.domain.companies.value_objects.PictureOfCards;

import static org.project.karto.domain.common.util.Utils.required;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
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
    private @Nullable PictureOfCards picture;

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

        if (id == null)
            throw new IllegalDomainArgumentException("id must not be null");
        if (registrationNumber == null)
            throw new IllegalDomainArgumentException("registrationNumber must not be null");
        if (companyName == null)
            throw new IllegalDomainArgumentException("companyName must not be null");
        if (email == null)
            throw new IllegalDomainArgumentException("email must not be null");
        if (phone == null)
            throw new IllegalDomainArgumentException("phone must not be null");
        if (creationDate == null)
            throw new IllegalDomainArgumentException("creationDate must not be null");
        if (lastUpdated == null)
            throw new IllegalDomainArgumentException("lastUpdate must not be null");
        if (password == null)
            throw new IllegalDomainArgumentException("password must not be null");
        if (keyAndCounter == null)
            throw new IllegalDomainArgumentException("keyAndCounter must not be null");
        if (companyStatus == null)
            throw new IllegalDomainArgumentException("companyStatus must not be null");
        if (cardUsageLimitation == null)
            throw new IllegalDomainArgumentException("cardUsagesLimitation must not be null");

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

    public Optional<PictureOfCards> picture() {
        return Optional.ofNullable(picture);
    }

    public void changePicture(PictureOfCards picture) {
        required("picture", picture);
        if (!isActive())
            throw new IllegalDomainStateException("Company account must be verified.");

        this.picture = picture;
    }

    public boolean isActive() {
        return companyStatus == CompanyStatus.ACTIVE;
    }

    public void enable() {
        if (isActive())
            throw new IllegalDomainStateException("You can`t active already verified user.");
        if (keyAndCounter.counter() == 0)
            throw new IllegalDomainStateException(
                    "It is prohibited to activate an account that has not been verified.");

        this.companyStatus = CompanyStatus.ACTIVE;
        touch();
    }

    public void incrementCounter() {
        this.keyAndCounter = new KeyAndCounter(this.keyAndCounter.key(), this.keyAndCounter.counter() + 1);
        touch();
    }

    public void changePassword(Password password) {
        if (password == null)
            throw new IllegalDomainArgumentException("Password can`t be null");
        if (companyStatus != CompanyStatus.ACTIVE)
            throw new IllegalDomainArgumentException("Company account is not verified");

        this.password = password;
        touch();
    }

    public CardUsageLimitations cardUsageLimitation() {
        return cardUsageLimitation;
    }

    public void specifyCardUsageLimitations(CardUsageLimitations cardUsageLimitations) {
        if (cardUsageLimitations == null)
            throw new IllegalDomainArgumentException("Card usage limitations can`t be null");
        if (companyStatus != CompanyStatus.ACTIVE)
            throw new IllegalDomainArgumentException("Company account is not verified");

        this.cardUsageLimitation = cardUsageLimitations;
        touch();
    }

    private void touch() {
        this.lastUpdated = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        Company company = (Company) o;
        return Objects.equals(id, company.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
