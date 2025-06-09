package org.project.karto.domain.card.repositories;

import com.hadzhy.jetquerious.util.Result;
import org.project.karto.domain.card.entities.CardVerificationOTP;
import org.project.karto.domain.card.value_objects.CardID;
import org.project.karto.domain.card.value_objects.OwnerID;

public interface CardVerificationOTPRepository {

    Result<Integer, Throwable> save(CardVerificationOTP otp);

    Result<Integer, Throwable> update(CardVerificationOTP otp);

    Result<Integer, Throwable> remove(CardVerificationOTP otp);

    Result<CardVerificationOTP, Throwable> findBy(CardVerificationOTP otp);

    Result<CardVerificationOTP, Throwable> findBy(CardID cardID);

    Result<CardVerificationOTP, Throwable> findBy(String otp);

    Result<CardVerificationOTP, Throwable> findBy(OwnerID ownerID);
}
