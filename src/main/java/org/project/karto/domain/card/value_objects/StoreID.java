package org.project.karto.domain.card.value_objects;

import java.util.UUID;

public record StoreID(UUID value) {
    public StoreID {
        if (value == null)
            throw new IllegalArgumentException("Store id value cannot be null");
    }

    public static StoreID fromString(String storeID) {
        return new StoreID(UUID.fromString(storeID));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
