package org.project.karto.domain.companies.repository;

import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.common.value_objects.Email;
import org.project.karto.domain.common.value_objects.Phone;
import org.project.karto.domain.companies.entities.Company;
import org.project.karto.domain.companies.value_objects.RegistrationNumber;

import java.util.UUID;

public interface CompanyRepository {

    void save(Company company);

    void update(Company company);

    void updatePassword(Company company);

    Result<Company, Throwable> findBy(UUID companyID);

    Result<Company, Throwable> findBy(RegistrationNumber registrationNumber);

    Result<Company, Throwable> findBy(Phone phone);

    Result<Company, Throwable> findBy(Email email);
}
