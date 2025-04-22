package org.project.karto.infrastructure.security;

import io.quarkus.logging.Log;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.karto.domain.user.entities.User;

import java.time.Duration;
import java.util.Optional;

@Singleton
public class JwtUtility {

    private final JWTParser jwtParser;

    public JwtUtility(JWTParser jwtParser) {
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

    public Optional<JsonWebToken> parseJWT(String token) {
        try {
            return Optional.of(jwtParser.parse(token));
        } catch (ParseException e) {
            Log.error("Can`t parse jwt.", e);
        }

        return Optional.empty();
    }
}