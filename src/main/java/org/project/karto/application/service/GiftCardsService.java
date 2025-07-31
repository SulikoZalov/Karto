package org.project.karto.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response.Status;

import java.util.List;

import org.project.karto.application.dto.gift_card.CardDTO;
import org.project.karto.application.dto.gift_card.CardForm;
import org.project.karto.application.dto.gift_card.SpendRequest;
import org.project.karto.application.pagination.PageRequest;
import org.project.karto.domain.card.repositories.CardVerificationOTPRepository;
import org.project.karto.domain.card.repositories.GiftCardRepository;
import org.project.karto.domain.common.value_objects.Amount;
import org.project.karto.domain.common.value_objects.Email;
import org.project.karto.domain.common.value_objects.Language;
import org.project.karto.domain.user.entities.User;
import org.project.karto.domain.user.repository.UserRepository;

import static org.project.karto.domain.common.util.Utils.required;
import static org.project.karto.application.util.RestUtil.responseException;

@ApplicationScoped
public class GiftCardsService {

    private final UserRepository userRepository;

    private final GiftCardRepository giftCardRepository;

    private final CardVerificationOTPRepository cardVerificationRepository;

    GiftCardsService(
            UserRepository userRepository,
            GiftCardRepository giftCardRepository,
            CardVerificationOTPRepository cardVerificationRepository) {
        this.userRepository = userRepository;
        this.giftCardRepository = giftCardRepository;
        this.cardVerificationRepository = cardVerificationRepository;
    }

    public void create(CardForm cardForm, Email email) {
        if (cardForm == null)
            throw responseException(Status.BAD_REQUEST, "Card form is required.");

        User user = userRepository.findBy(email).orElseThrow();

    }

    public void verify(String otp, Email email) {
        // TODO
    }

    public void spend(SpendRequest spendRequest, Email email) {
        // TODO
    }

    public List<CardDTO> availableGiftCards(PageRequest pageRequest) {
        required("pageRequest", pageRequest);
        return giftCardRepository.availableGiftCards(pageRequest)
                .orElseThrow(() -> responseException(Status.BAD_REQUEST, "No available gift cards on this page."));
    }
}
