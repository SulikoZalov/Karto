package org.project.karto.infrastructure.security;

import io.quarkus.logging.Log;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.user.entities.User;

import java.time.Duration;

@Singleton
public class JWTUtility {

    private final JWTParser jwtParser;

    private static final JwtConsumer jwtConsumer = new JwtConsumerBuilder()
            .setSkipSignatureVerification()
            .setSkipAllValidators()
            .build();

    public JWTUtility(JWTParser jwtParser) {
        this.jwtParser = jwtParser;
    }

    public String generateToken(User user) {
        Duration oneDayAndSecond = Duration.ofDays(1).plusSeconds(1);

        return Jwt.issuer("Chessland")
                .upn(user.personalData().email())
                .claim("firstname", user.personalData().firstname())
                .claim("surname", user.personalData().surname())
                .claim("isVerified", user.isVerified())
                .expiresIn(oneDayAndSecond)
                .sign();
    }

    public String generateRefreshToken(User user) {
        Duration year = Duration.ofDays(365);

        return Jwt.issuer("Chessland")
                .upn(user.personalData().email())
                .expiresIn(year)
                .sign();
    }

    public Result<JsonWebToken, Throwable> parse(String token) {
        try {
            return Result.success(jwtParser.parse(token));
        } catch (ParseException e) {
            Log.error("Can`t parse jwt.", e);
            return Result.failure(e);
        }
    }

    public Result<JwtClaims, Throwable> parseUnverified(String jwt) {
        try {
            return Result.success(jwtConsumer.processToClaims(jwt));
        } catch (InvalidJwtException e) {
            return Result.failure(e);
        }
    }
}