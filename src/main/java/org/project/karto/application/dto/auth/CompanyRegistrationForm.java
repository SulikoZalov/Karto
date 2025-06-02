package org.project.karto.application.dto.auth;

public record CompanyRegistrationForm(
    String registrationCountryCode,
    String registrationNumber,
    String companyName,
    String email,
    String phone,
    String rawPassword,
    int cardExpirationDays,
    int cardMaxUsageCount
) {}
