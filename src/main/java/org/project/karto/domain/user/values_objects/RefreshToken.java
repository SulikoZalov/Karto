package org.project.karto.domain.user.values_objects;

import java.util.UUID;

public record RefreshToken(UUID userID, String refreshToken) {

    public RefreshToken {
        if (userID == null || refreshToken == null)
            throw new IllegalArgumentException("User id or refresh token is null");
    }
}
