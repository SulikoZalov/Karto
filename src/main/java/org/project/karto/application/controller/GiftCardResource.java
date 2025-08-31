package org.project.karto.application.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.project.karto.application.dto.gift_card.CardDTO;
import org.project.karto.application.pagination.PageRequest;
import org.project.karto.application.service.GiftCardsService;

import java.util.List;

@Path("/gift-card")
@RolesAllowed("CUSTOMER")
public class GiftCardResource {

    private final GiftCardsService giftCardsService;

    GiftCardResource(GiftCardsService giftCardsService) {
        this.giftCardsService = giftCardsService;
    }

    @GET
    @Path("/available/gift_cards")
    public List<CardDTO> availableGiftCards(@QueryParam("limit") int limit, @QueryParam("offset") int offset) {
        return giftCardsService.availableGiftCards(new PageRequest(offset, limit));
    }
}
