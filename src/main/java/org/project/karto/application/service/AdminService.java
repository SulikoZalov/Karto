package org.project.karto.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.project.karto.application.dto.auth.CompanyRegistrationForm;
import org.project.karto.application.dto.auth.Token;
import org.project.karto.domain.common.value_objects.CardUsageLimitations;
import org.project.karto.domain.common.value_objects.Email;
import org.project.karto.domain.common.value_objects.Phone;
import org.project.karto.domain.companies.entities.Company;
import org.project.karto.domain.companies.repository.CompanyRepository;
import org.project.karto.domain.companies.value_objects.CompanyName;
import org.project.karto.domain.companies.value_objects.RegistrationNumber;
import org.project.karto.domain.user.values_objects.Password;
import org.project.karto.infrastructure.security.JWTUtility;
import org.project.karto.infrastructure.security.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.project.karto.application.util.RestUtil.responseException;

@ApplicationScoped
public class AdminService {

    @ConfigProperty(name = "admin.verification.key")
    String verificationKey;

    private final JWTUtility jwtUtility;

    private final PasswordEncoder passwordEncoder;

    private final CompanyRepository companyRepository;

    AdminService(JWTUtility jwtUtility, PasswordEncoder passwordEncoder, CompanyRepository companyRepository) {
        this.jwtUtility = jwtUtility;
        this.passwordEncoder = passwordEncoder;
        this.companyRepository = companyRepository;
    }

    public Token auth(String verificationKey) {
        if (secureEquals(this.verificationKey, verificationKey))
            throw responseException(Response.Status.FORBIDDEN, "Invalid administrator verification key.");

        return new Token(jwtUtility.generateAdministratorToken());
    }

    private boolean secureEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    public void registerPartner(CompanyRegistrationForm registrationForm) {
        if (registrationForm == null)
            throw responseException(Response.Status.BAD_REQUEST, "Company registration form must be filled.");

        Password.validate(registrationForm.rawPassword());

        RegistrationNumber registrationNumber = new RegistrationNumber(registrationForm.registrationCountryCode(),
                registrationForm.registrationNumber());
        if (companyRepository.isExists(registrationNumber))
            throw responseException(Response.Status.CONFLICT, "Registration number already exists.");

        Phone phone = new Phone(registrationForm.phone());
        if (companyRepository.isExists(phone))
            throw responseException(Response.Status.CONFLICT, "Phone already exists.");

        Email email = new Email(registrationForm.email());
        if (companyRepository.isExists(email))
            throw responseException(Response.Status.CONFLICT, "Email already exists.");

        Password encodedPassword = new Password(passwordEncoder.encode(registrationForm.rawPassword()));

        Company company = Company.of(
                registrationNumber,
                new CompanyName(registrationForm.companyName()),
                email,
                phone,
                encodedPassword,
                CardUsageLimitations.of(registrationForm.cardExpirationDays(), registrationForm.cardMaxUsageCount())
        );

        companyRepository.save(company);
    }
}
