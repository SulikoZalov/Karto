package org.project.karto.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.project.karto.application.dto.auth.Token;
import org.project.karto.infrastructure.security.JWTUtility;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.project.karto.application.util.RestUtil.responseException;

@ApplicationScoped
public class AdminService {

    @ConfigProperty(name = "admin.verification.key")
    String verificationKey;

    private final JWTUtility jwtUtility;

    AdminService(JWTUtility jwtUtility) {
        this.jwtUtility = jwtUtility;
    }

    public Token auth(String verificationKey) {
        if (secureEquals(this.verificationKey, verificationKey))
            throw responseException(Response.Status.FORBIDDEN, "Invalid administrator verification key.");

        return new Token(jwtUtility.generateAdministratorToken());
    }

    private boolean secureEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }


}
