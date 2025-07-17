package org.project.karto.domain.card.value_objects;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

import java.util.UUID;

public record CardID(UUID value) {
    public CardID {
        if (value == null)
            throw new IllegalDomainArgumentException("Card id can`t be null");
    }

    public static CardID fromString(String uuidStr) {
        return new CardID(UUID.fromString(uuidStr));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
