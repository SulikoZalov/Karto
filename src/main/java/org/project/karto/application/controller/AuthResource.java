package org.project.karto.application.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.project.karto.application.dto.auth.LateVerificationForm;
import org.project.karto.application.dto.auth.LoginForm;
import org.project.karto.application.dto.auth.RegistrationForm;
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
    public Response oidcAuth(@HeaderParam("X-ID-TOKEN") String idToken) {
        return Response.ok(authService.oidcAuth(idToken)).build();
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
    public Response enable2FA(LoginForm loginForm) {
        authService.enable2FA(loginForm);
        return Response.accepted("Confirm the OTP sent to you via SMS to complete the two-factor authentication confirmation")
                .build();
    }

    @PATCH
    @Path("/2FA/verify")
    public Response verify2FA(@QueryParam("otp") String otp) {
        return Response.accepted(authService.twoFactorAuth(otp)).build();
    }

    @POST
    @Path("/login")
    public Response login(LoginForm loginForm) {
        return Response.ok(authService.login(loginForm)).build();
    }

    @PATCH
    @Path("/refresh-token")
    public Response refresh(@HeaderParam("Refresh-Token") String refreshToken) {
        return Response.ok(authService.refreshToken(refreshToken)).build();
    }
}
