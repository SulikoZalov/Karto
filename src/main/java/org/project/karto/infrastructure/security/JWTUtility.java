package org.project.karto.infrastructure.security;

import io.quarkus.logging.Log;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.auth.principal.ParseException;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Singleton;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.common.enumerations.Role;
import org.project.karto.domain.user.entities.User;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;

@Singleton
public class JWTUtility {

    private final JWTParser jwtParser;

    private static final RSAPublicKey keycloackPublicKey = readX509PublicKey();

    public JWTUtility(JWTParser jwtParser) {
        this.jwtParser = jwtParser;
    }

    public String generateToken(User user) {
        Duration oneDayAndSecond = Duration.ofDays(1).plusSeconds(1);

        return Jwt.issuer("Karto")
                .upn(user.personalData().email())
                .groups(user.role().name())
                .claim("firstname", user.personalData().firstname())
                .claim("surname", user.personalData().surname())
                .claim("isVerified", user.isVerified())
                .expiresIn(oneDayAndSecond)
                .sign();
    }

    public String generateAdministratorToken() {
        return Jwt.issuer("Karto")
                .groups(Role.ADMIN.name())
                .expiresIn(Duration.ofMinutes(10))
                .sign();
    }

    public String generateRefreshToken(User user) {
        Duration year = Duration.ofDays(365);

        return Jwt.issuer("Karto")
                .upn(user.personalData().email())
                .groups(user.role().name())
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

    public Result<JsonWebToken, Throwable> verifyAndParse(String jwt) {
        try {
            JsonWebToken verified = jwtParser.verify(jwt, keycloackPublicKey);
            return Result.success(verified);
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    private static RSAPublicKey readX509PublicKey() {
        try {
            String key = Files.readString(Path.of("src/main/resources/keycloackPublicKey.pem"), Charset.defaultCharset())
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll(System.lineSeparator(), "")
                    .replace("-----END PUBLIC KEY-----", "");

            byte[] encoded = Base64.decodeBase64(key);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException(e);
        }
    }
}