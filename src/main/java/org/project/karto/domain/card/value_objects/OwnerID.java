package org.project.karto.domain.card.value_objects;

import java.util.UUID;

public record OwnerID(UUID value) {
    public OwnerID {
        if (value == null)
            throw new IllegalStateException("Owner ID can't be null");
    }
    public static OwnerID fromString(String uuidStr) {
        return new OwnerID(UUID.fromString(uuidStr));
    }
}
