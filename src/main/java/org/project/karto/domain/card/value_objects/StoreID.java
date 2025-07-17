package org.project.karto.domain.card.value_objects;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

import java.util.UUID;

public record StoreID(UUID value) {
    public StoreID {
        if (value == null)
            throw new IllegalDomainArgumentException("Store id value cannot be null");
    }

    public static StoreID fromString(String storeID) {
        return new StoreID(UUID.fromString(storeID));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
