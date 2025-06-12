package org.project.karto.domain.companies.repository;

import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.common.value_objects.Email;
import org.project.karto.domain.common.value_objects.Phone;
import org.project.karto.domain.companies.entities.Company;
import org.project.karto.domain.companies.value_objects.CompanyName;
import org.project.karto.domain.companies.value_objects.RegistrationNumber;

import java.util.UUID;

public interface CompanyRepository {

    Result<Integer, Throwable> save(Company company);

    Result<Integer, Throwable> updateCardUsageLimitations(Company company);

    Result<Integer, Throwable> updatePassword(Company company);

    Result<Integer, Throwable> updateCounter(Company company);

    Result<Integer, Throwable> updateVerification(Company company);

    Result<Company, Throwable> findBy(UUID companyID);

    Result<Company, Throwable> findBy(RegistrationNumber registrationNumber);

    Result<Company, Throwable> findBy(Phone phone);

    Result<Company, Throwable> findBy(Email email);

    Result<Company, Throwable> findBy(CompanyName companyName);

    boolean isExists(RegistrationNumber registrationNumber);

    boolean isExists(Phone phone);

    boolean isExists(Email email);

    boolean isExists(CompanyName companyName);
}
