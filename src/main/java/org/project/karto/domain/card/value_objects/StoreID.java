package org.project.karto.domain.card.value_objects;

import java.util.UUID;

public record StoreID(UUID value) {
    public StoreID {
        if (value == null)
            throw new IllegalStateException("Store ID can't be null");
    }
    public static StoreID fromString(String uuidStr) {
        return new StoreID(UUID.fromString(uuidStr));
    }
}
