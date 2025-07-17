package org.project.karto.domain.card.value_objects;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

import java.util.UUID;

public record OwnerID(UUID value) {
    public OwnerID {
        if (value == null)
            throw new IllegalDomainArgumentException("Owner ID can't be null");
    }

    public static OwnerID fromString(String uuidStr) {
        return new OwnerID(UUID.fromString(uuidStr));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
