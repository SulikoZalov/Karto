package org.project.karto.application.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.karto.application.dto.gift_card.CardForm;
import org.project.karto.application.service.GiftCardsService;
import org.project.karto.domain.common.value_objects.Email;

@Path("/gift-card")
@RolesAllowed("CUSTOMER")
public class GiftCardResource {

    private final JsonWebToken jwt;

    private final GiftCardsService giftCardsService;

    GiftCardResource(JsonWebToken jwt, GiftCardsService giftCardsService) {
        this.jwt = jwt;
        this.giftCardsService = giftCardsService;
    }

    @POST
    @Path("/create")
    public Response create(CardForm cardForm) {
        giftCardsService.create(cardForm, new Email(jwt.getName()));
        return Response.accepted("Gift card successfully created.").build();
    }

    @POST
    @Path("/spend")
    public Response spend() {
        giftCardsService.spend();
        return Response.accepted("Transaction successfully commited.").build();
    }
}

