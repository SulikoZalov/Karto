package org.project.karto.domain.card.repositories;

import org.project.karto.domain.card.entities.GiftCard;
import org.project.karto.domain.card.value_objects.BuyerID;
import org.project.karto.domain.card.value_objects.CardID;
import org.project.karto.domain.card.value_objects.OwnerID;
import org.project.karto.domain.card.value_objects.StoreID;
import org.project.karto.domain.common.containers.Result;

import java.util.List;

public interface GiftCardRepository {

    void save(GiftCard giftCard);

    void update(GiftCard giftCard);

    Result<GiftCard, Throwable> findBy(CardID cardID);

    Result<List<GiftCard>, Throwable> findBy(BuyerID buyerID);

    Result<List<GiftCard>, Throwable> findBy(OwnerID ownerID);

    Result<List<GiftCard>, Throwable> findBy(StoreID storeID);
}
