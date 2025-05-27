package org.project.karto.application.dto.gift_card;

import org.project.karto.domain.card.enumerations.GiftCardRecipientType;
import org.project.karto.domain.card.enumerations.GiftCardType;
import org.project.karto.domain.card.value_objects.StoreID;

public record CardForm(GiftCardRecipientType recipientType, GiftCardType cardType, StoreID store) {}