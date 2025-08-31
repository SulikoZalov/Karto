package org.project.karto.application.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.inject.Instance;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.karto.application.dto.auth.LoginForm;
import org.project.karto.application.dto.auth.Token;
import org.project.karto.application.service.CompanyService;

@Path("/partner")
public class PartnerResource {

    private final JsonWebToken jwt;

    private final CompanyService companyService;

    PartnerResource(Instance<JsonWebToken> jwt, CompanyService companyService) {
        this.jwt = jwt.get();
        this.companyService = companyService;
    }

    @GET
    @Path("/otp/resend")
    public Response resendOTP(@QueryParam("phoneNumber") String phoneNumber) {
        companyService.resendPartnerOTP(phoneNumber);
        return Response.ok().build();
    }

    @PATCH
    @Path("/verification")
    public Response verifyPartnerAccount(@QueryParam("otp") String otp) {
        companyService.verifyPartnerAccount(otp);
        return Response.accepted().build();
    }

    @POST
    @Path("/login")
    public Token login(LoginForm loginForm) {
        return companyService.login(loginForm);
    }

    @PATCH
    @Path("/password/change")
    @RolesAllowed("PARTNER")
    public Response changePassword(@QueryParam("newPassword") String rawPassword) {
        companyService.changePassword(rawPassword, jwt.getName());
        return Response.accepted().build();
    }

    @PATCH
    @Path("/card/limitations")
    @RolesAllowed("PARTNER")
    public Response changeCardLimitations(@QueryParam("expiration") int days,
            @QueryParam("maxUsageCount") int maxUsageCount) {
        companyService.changeCardLimitations(days, maxUsageCount, jwt.getName());
        return Response.accepted().build();
    }
}
