package org.project.karto.domain.companies.repository;

import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.companies.entities.PartnerVerificationOTP;

import java.util.UUID;

public interface PartnerVerificationOTPRepository {

    void save(PartnerVerificationOTP otp);

    void update(PartnerVerificationOTP otp);

    void remove(PartnerVerificationOTP otp);

    void updateConfirmation(PartnerVerificationOTP otp);

    Result<PartnerVerificationOTP, Throwable> findBy(PartnerVerificationOTP otp);

    Result<PartnerVerificationOTP, Throwable> findBy(UUID companyID);

    Result<PartnerVerificationOTP, Throwable> findBy(String otp);
}
