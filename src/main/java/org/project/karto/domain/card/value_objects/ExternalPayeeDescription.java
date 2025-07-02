package org.project.karto.domain.card.value_objects;

public record ExternalPayeeDescription(String value) {

    public ExternalPayeeDescription {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("ExternalPayeeDescription must not be null or blank");
        value = value.strip();
    }

    @Override
    public String toString() {
        return value;
    }
}
