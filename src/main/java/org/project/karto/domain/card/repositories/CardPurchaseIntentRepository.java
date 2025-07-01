package org.project.karto.domain.card.repositories;

import org.project.karto.domain.card.entities.CardPurchaseIntent;
import org.project.karto.domain.card.value_objects.BuyerID;
import org.project.karto.domain.common.containers.Result;

import java.util.UUID;

public interface CardPurchaseIntentRepository {

    Result<Integer, Throwable> save(CardPurchaseIntent purchaseIntent);

    Result<Integer, Throwable> update(CardPurchaseIntent purchaseIntent);

    Result<CardPurchaseIntent, Throwable> findBy(UUID id);

    Result<CardPurchaseIntent, Throwable> findBy(BuyerID buyerID);

    Result<CardPurchaseIntent, Throwable> findBy(long orderID);
}
