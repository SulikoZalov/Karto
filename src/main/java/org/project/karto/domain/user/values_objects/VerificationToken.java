package org.project.karto.domain.user.values_objects;

public record VerificationToken(long token) {
    public static VerificationToken instance(long token) {
        return new VerificationToken(token);
    }
}