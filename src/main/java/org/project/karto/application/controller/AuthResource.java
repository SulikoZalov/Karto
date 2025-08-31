package org.project.karto.application.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.project.karto.application.dto.auth.*;
import org.project.karto.application.dto.common.Info;
import org.project.karto.application.service.AuthService;

@Path("/auth")
public class AuthResource {

    private final AuthService authService;

    AuthResource(AuthService authService) {
        this.authService = authService;
    }

    @POST
    @Path("/registration")
    public Response registration(RegistrationForm registrationForm) {
        authService.registration(registrationForm);
        return Response.accepted().build();
    }

    @POST
    @Path("/oidc")
    public Tokens oidcAuth(@HeaderParam("X-ID-TOKEN") String idToken) {
        return authService.oidcAuth(idToken);
    }

    @GET
    @Path("/resend-otp")
    public Response resendOTP(@QueryParam("phoneNumber") String phoneNumber) {
        authService.resendOTP(phoneNumber);
        return Response.ok().build();
    }

    @PATCH
    @Path("/late-verification")
    public Response lateVerification(LateVerificationForm lvForm) {
        authService.lateVerification(lvForm);
        return Response.accepted().build();
    }

    @PATCH
    @Path("/verification")
    public Response verification(@QueryParam("otp") String otp) {
        authService.verification(otp);
        return Response.accepted().build();
    }

    @POST
    @Path("/2FA/enable")
    public Info enable2FA(LoginForm loginForm) {
        authService.enable2FA(loginForm);
        return new Info("Confirm the OTP sent to you via SMS to complete the two-factor authentication confirmation");
    }

    @PATCH
    @Path("/2FA/verify")
    public Tokens verify2FA(@QueryParam("otp") String otp) {
        return authService.twoFactorAuth(otp);
    }

    @POST
    @Path("/login")
    public LoginResponse login(LoginForm loginForm) {
        return authService.login(loginForm);
    }

    @PATCH
    @Path("/refresh-token")
    public Token refresh(@HeaderParam("Refresh-Token") String refreshToken) {
        return authService.refreshToken(refreshToken);
    }
}
