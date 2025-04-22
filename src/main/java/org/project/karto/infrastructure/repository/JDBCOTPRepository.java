package org.project.karto.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.user.repository.OTPRepository;
import org.project.karto.domain.user.entities.OTP;

import java.util.UUID;

@ApplicationScoped
public class JDBCOTPRepository implements OTPRepository {

    // TODO

    @Override
    public void save(OTP otp) {

    }

    @Override
    public void update(OTP otp) {

    }

    @Override
    public Result<OTP, Throwable> findBy(OTP otp) {
        return null;
    }

    @Override
    public Result<OTP, Throwable> findBy(String otp) {
        return null;
    }

    @Override
    public Result<OTP, Throwable> findBy(UUID userID) {
        return null;
    }

    @Override
    public void remove(OTP otp) {

    }
}
