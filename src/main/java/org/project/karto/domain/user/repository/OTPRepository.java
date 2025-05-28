package org.project.karto.domain.user.repository;

import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.user.entities.OTP;

import java.util.UUID;

public interface OTPRepository {

    void save(OTP otp);

    void updateConfirmation(OTP otp);

    void remove(OTP otp);

    boolean contains(UUID user_id);

    Result<OTP, Throwable> findBy(OTP otp);

    Result<OTP, Throwable> findBy(String otp);

    Result<OTP, Throwable> findBy(UUID userID);
}
