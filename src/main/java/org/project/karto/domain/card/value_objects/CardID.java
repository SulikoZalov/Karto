package org.project.karto.domain.card.value_objects;

import java.util.UUID;

public record CardID(UUID value) {
    public CardID {
        if (value == null)
            throw new IllegalStateException("Card id can`t be null");
    }
}
