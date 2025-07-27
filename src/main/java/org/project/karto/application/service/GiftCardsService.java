package org.project.karto.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response.Status;

import java.util.List;

import org.project.karto.application.dto.gift_card.CardDTO;
import org.project.karto.application.dto.gift_card.CardForm;
import org.project.karto.application.pagination.PageRequest;
import org.project.karto.domain.card.repositories.CardVerificationOTPRepository;
import org.project.karto.domain.card.repositories.GiftCardRepository;
import org.project.karto.domain.common.value_objects.Amount;
import org.project.karto.domain.common.value_objects.Email;
import org.project.karto.domain.common.value_objects.Language;

import static org.project.karto.domain.common.util.Utils.required;
import static org.project.karto.application.util.RestUtil.responseException;

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

    public void verify(String otp, Email email) {
        // TODO
    }

    public void spend(Amount amount, Language language, Email email) {
        // TODO
    }

    public List<CardDTO> availableGiftCards(PageRequest pageRequest) {
        required("pageRequest", pageRequest);
        return giftCardRepository.availableGiftCards(pageRequest)
                .orElseThrow(() -> responseException(Status.BAD_REQUEST, "No available gift cards on this page."));
    }
}
