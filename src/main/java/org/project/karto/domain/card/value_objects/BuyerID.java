package org.project.karto.domain.card.value_objects;

import java.util.UUID;

public record BuyerID(UUID value) {
    public BuyerID {
        if (value == null)
            throw new IllegalArgumentException("Buyer ID can't be null");
    }

    public static BuyerID fromString(String uuidStr) {
        return new BuyerID(UUID.fromString(uuidStr));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
