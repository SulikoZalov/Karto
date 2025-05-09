package org.project.karto.domain.card.repositories;

import org.project.karto.domain.card.entities.CardVerificationOTP;
import org.project.karto.domain.card.value_objects.CardID;
import org.project.karto.domain.card.value_objects.OwnerID;
import org.project.karto.domain.common.containers.Result;

public interface CardVerificationOTPRepository {

    void save(CardVerificationOTP otp);

    void update(CardVerificationOTP otp);

    void remove(CardVerificationOTP otp);

    Result<CardVerificationOTP, Throwable> findBy(CardVerificationOTP otp);

    Result<CardVerificationOTP, Throwable> findBy(CardID cardID);

    Result<CardVerificationOTP, Throwable> findBy(String otp);

    Result<CardVerificationOTP, Throwable> findBy(OwnerID ownerID);
}
