package org.project.karto.domain.card.value_objects;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

public record ExternalPayeeDescription(String value) {
    private static final int MAX_SIZE = 255;

    public ExternalPayeeDescription {
        if (value == null || value.isBlank())
            throw new IllegalDomainArgumentException("ExternalPayeeDescription must not be null or blank");
        if (value.length() > MAX_SIZE) throw new IllegalDomainArgumentException("Description is too long");

        value = value.strip();
    }

    @Override
    public String toString() {
        return value;
    }
}
