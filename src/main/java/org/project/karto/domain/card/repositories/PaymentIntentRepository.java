package org.project.karto.domain.card.repositories;

import org.project.karto.domain.card.entities.PaymentIntent;
import org.project.karto.domain.common.containers.Result;

import java.util.UUID;

public interface PaymentIntentRepository {

    Result<Integer, Throwable> save(PaymentIntent paymentIntent);

    Result<Integer, Throwable> update(PaymentIntent paymentIntent);

    Result<PaymentIntent, Throwable> findBy(UUID id);

    Result<PaymentIntent, Throwable> findBy(long orderID);
}
