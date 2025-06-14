package org.project.karto.domain.companies.repository;

import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.companies.entities.PartnerVerificationOTP;

import java.util.UUID;

public interface PartnerVerificationOTPRepository {

    Result<Integer, Throwable> save(PartnerVerificationOTP otp);

    Result<Integer, Throwable> remove(PartnerVerificationOTP otp);

    Result<Integer, Throwable> updateConfirmation(PartnerVerificationOTP otp);

    Result<PartnerVerificationOTP, Throwable> findBy(PartnerVerificationOTP otp);

    Result<PartnerVerificationOTP, Throwable> findBy(UUID companyID);

    Result<PartnerVerificationOTP, Throwable> findBy(String otp);
}
