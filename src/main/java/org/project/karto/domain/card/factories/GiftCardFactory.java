package org.project.karto.domain.card.factories;

import org.project.karto.domain.card.entities.CardPurchaseIntent;
import org.project.karto.domain.card.entities.GiftCard;
import org.project.karto.domain.card.enumerations.GiftCardRecipientType;
import org.project.karto.domain.card.enumerations.PurchaseStatus;
import org.project.karto.domain.card.value_objects.Balance;
import org.project.karto.domain.card.value_objects.StoreID;
import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;
import org.project.karto.domain.common.value_objects.Amount;
import org.project.karto.domain.common.value_objects.CardUsageLimitations;

import java.util.function.Function;

import static org.project.karto.domain.common.util.Utils.required;

public class GiftCardFactory {

    private GiftCardFactory() {}

    @SuppressWarnings("unchecked")
    private static final Function<CardCreationParams, GiftCard>[] CREATION_STRATEGIES = new Function[4];

    static {
        CREATION_STRATEGIES[0] = params -> GiftCard.selfBoughtCard(
                params.purchaseIntent.buyerID(),
                params.balance,
                params.storeID,
                params.secretKey,
                params.cardUsageLimitations
        );

        CREATION_STRATEGIES[1] = params -> GiftCard.selfBoughtCommonCard(
                params.purchaseIntent.buyerID(),
                params.balance,
                params.secretKey,
                params.cardUsageLimitations
        );

        CREATION_STRATEGIES[2] = params -> GiftCard.boughtAsAGift(
                params.purchaseIntent.buyerID(),
                params.balance,
                params.storeID,
                params.secretKey,
                params.cardUsageLimitations
        );

        CREATION_STRATEGIES[3] = params -> GiftCard.giftedCommonCard(
                params.purchaseIntent.buyerID(),
                params.balance,
                params.secretKey,
                params.cardUsageLimitations
        );
    }

    public static GiftCard createFromPurchaseIntent(
            CardPurchaseIntent purchaseIntent,
            GiftCardRecipientType recipientType,
            String secretKey,
            CardUsageLimitations cardUsageLimitations) {

        required("purchaseIntent", purchaseIntent);
        required("recipientType", recipientType);
        required("secretKey", secretKey);
        required("cardUsageLimitations", cardUsageLimitations);

        if (purchaseIntent.status() != PurchaseStatus.SUCCESS)
            throw new IllegalDomainArgumentException("Cannot create gift card from unsuccessful purchase intent");

        Amount netAmount = purchaseIntent.calculateNetAmount();
        Balance balance = new Balance(netAmount.value());

        StoreID storeID = purchaseIntent.storeID().orElse(null);

        int strategyIndex = calculateStrategyIndex(recipientType, storeID);
        CardCreationParams params = new CardCreationParams(purchaseIntent, balance, storeID, secretKey, cardUsageLimitations);

        return CREATION_STRATEGIES[strategyIndex].apply(params);
    }

    private static int calculateStrategyIndex(GiftCardRecipientType recipientType, StoreID storeID) {
        int recipientIndex = recipientType.ordinal();
        int storeTypeIndex = storeID != null ? 1 : 0;
        return (storeTypeIndex << 1) | recipientIndex;
    }

    private record CardCreationParams(
            CardPurchaseIntent purchaseIntent, Balance balance,
            StoreID storeID, String secretKey,
            CardUsageLimitations cardUsageLimitations) {}
}