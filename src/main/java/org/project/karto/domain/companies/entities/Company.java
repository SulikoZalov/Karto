package org.project.karto.domain.companies.entities;

import org.project.karto.domain.common.value_objects.CardUsageLimitations;
import org.project.karto.domain.common.value_objects.Email;
import org.project.karto.domain.common.value_objects.Phone;
import org.project.karto.domain.companies.value_objects.RegistrationNumber;
import org.project.karto.domain.companies.value_objects.CompanyName;
import org.project.karto.domain.user.values_objects.Password;

import java.time.LocalDateTime;
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
    private CardUsageLimitations cardUsagesLimitation;

    private Company(
            UUID id,
            RegistrationNumber registrationNumber,
            CompanyName companyName,
            Email email,
            Phone phone,
            LocalDateTime creationDate,
            LocalDateTime lastUpdated,
            Password password,
            CardUsageLimitations cardUsagesLimitation) {

        if (id == null) throw new IllegalArgumentException("id must not be null");
        if (registrationNumber == null) throw new IllegalArgumentException("registrationNumber must not be null");
        if (companyName == null) throw new IllegalArgumentException("companyName must not be null");
        if (email == null) throw new IllegalArgumentException("email must not be null");
        if (phone == null) throw new IllegalArgumentException("phone must not be null");
        if (creationDate == null) throw new IllegalArgumentException("creationDate must not be null");
        if (lastUpdated == null) throw new IllegalArgumentException("lastUpdate must not be null");
        if (password == null) throw new IllegalArgumentException("password must not be null");
        if (cardUsagesLimitation == null) throw new IllegalArgumentException("cardUsagesLimitation must not be null");

        this.id = id;
        this.registrationNumber = registrationNumber;
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
        this.creationDate = creationDate;
        this.lastUpdated = lastUpdated;
        this.password = password;
        this.cardUsagesLimitation = cardUsagesLimitation;
    }

    public static Company of(
            RegistrationNumber registrationNumber,
            CompanyName companyName,
            Email email,
            Phone phone,
            Password password,
            CardUsageLimitations cardUsagesLimitation) {

        LocalDateTime now = LocalDateTime.now();
        return new Company(UUID.randomUUID(), registrationNumber, companyName, email, phone, now, now, password, cardUsagesLimitation);
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
            CardUsageLimitations cardUsagesLimitation) {

        return new Company(id, registrationNumber, companyName, email,
                phone, creationDate, lastUpdate, password, cardUsagesLimitation);
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

    public void changePassword(Password password) {
        if (password == null)
            throw new IllegalArgumentException("Password can`t be null");

        this.password = password;
        this.lastUpdated = LocalDateTime.now();
    }

    public CardUsageLimitations cardUsagesLimitation() {
        return cardUsagesLimitation;
    }

    public void specifyCardUsageLimitations(CardUsageLimitations cardUsageLimitations) {
        if (cardUsageLimitations == null)
            throw new IllegalArgumentException("Card usage limitations can`t be null");

        this.cardUsagesLimitation = cardUsageLimitations;
        this.lastUpdated = LocalDateTime.now();
    }
}
