package org.project.karto.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.project.karto.application.dto.gift_card.CardForm;
import org.project.karto.domain.card.repositories.CardVerificationOTPRepository;
import org.project.karto.domain.card.repositories.GiftCardRepository;
import org.project.karto.domain.user.values_objects.Email;

@ApplicationScoped
public class GiftCardsService {

    private final GiftCardRepository giftCardRepository;

    private final CardVerificationOTPRepository cardVerificationRepository;

    GiftCardsService(GiftCardRepository giftCardRepository,
                     CardVerificationOTPRepository cardVerificationRepository) {
        this.giftCardRepository = giftCardRepository;
        this.cardVerificationRepository = cardVerificationRepository;
    }


    public void create(CardForm cardForm, Email email) {
        // TODO
    }

    public void spend() {
        // TODO
    }
}