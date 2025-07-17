package org.project.karto.domain.user.values_objects;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

import java.util.UUID;

public record RefreshToken(UUID userID, String refreshToken) {

    public RefreshToken {
        if (userID == null || refreshToken == null)
            throw new IllegalDomainArgumentException("User id or refresh token is null");
    }
}
