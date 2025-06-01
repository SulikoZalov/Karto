package org.project.karto.domain.companies.entities;

import org.project.karto.domain.common.value_objects.CardUsageLimitations;
import org.project.karto.domain.common.value_objects.Email;
import org.project.karto.domain.common.value_objects.Phone;
import org.project.karto.domain.companies.enumerations.CompanyStatus;
import org.project.karto.domain.companies.value_objects.RegistrationNumber;
import org.project.karto.domain.companies.value_objects.CompanyName;

import java.time.LocalDateTime;
import java.util.UUID;

public class Company {
    private final UUID id;
    private final RegistrationNumber registrationNumber;
    private final CompanyName companyName;
    private final Email email;
    private final Phone phone;
    private final LocalDateTime creationDate;
    private LocalDateTime lastUpdate;
    private CompanyStatus companyStatus;
    private CardUsageLimitations cardUsagesLimitation;

    private Company(
            UUID id,
            RegistrationNumber registrationNumber,
            CompanyName companyName,
            Email email,
            Phone phone,
            LocalDateTime creationDate,
            LocalDateTime lastUpdate,
            CompanyStatus companyStatus,
            CardUsageLimitations cardUsagesLimitation) {

        this.id = id;
        this.registrationNumber = registrationNumber;
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
        this.creationDate = creationDate;
        this.lastUpdate = lastUpdate;
        this.companyStatus = companyStatus;
        this.cardUsagesLimitation = cardUsagesLimitation;
    }
}
