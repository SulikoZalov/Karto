package org.project.karto.domain.user.repository;

import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.user.entities.OTP;

import java.util.UUID;

public interface OTPRepository {

    Result<Integer, Throwable> save(OTP otp);

    Result<Integer, Throwable> updateConfirmation(OTP otp);

    Result<Integer, Throwable> remove(OTP otp);

    boolean contains(UUID userID);

    Result<OTP, Throwable> findBy(OTP otp);

    Result<OTP, Throwable> findBy(String otp);

    Result<OTP, Throwable> findBy(UUID userID);
}
