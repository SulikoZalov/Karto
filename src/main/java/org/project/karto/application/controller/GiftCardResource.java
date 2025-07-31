package org.project.karto.application.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.UUID;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.karto.application.dto.gift_card.CardForm;
import org.project.karto.application.dto.gift_card.SpendRequest;
import org.project.karto.application.pagination.PageRequest;
import org.project.karto.application.service.GiftCardsService;
import org.project.karto.domain.common.value_objects.Amount;
import org.project.karto.domain.common.value_objects.Email;
import org.project.karto.domain.common.value_objects.Language;

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

    @PATCH
    @Path("/verify")
    public Response verify(@QueryParam("otp") String otp) {
        giftCardsService.verify(otp, new Email(jwt.getName()));
        return Response.accepted().build();
    }

    @POST
    @Path("/spend/manual")
    public Response spend(SpendRequest spendRequest) {
        giftCardsService.spend(spendRequest, new Email(jwt.getName()));
        return Response.accepted("Transaction successfully commited.").build();
    }

    @GET
    @Path("/available/gift_cards")
    public Response availableGiftCards(@QueryParam("limit") int limit, @QueryParam("offset") int offset) {
        return Response.ok(giftCardsService.availableGiftCards(new PageRequest(offset, limit))).build();
    }
}
