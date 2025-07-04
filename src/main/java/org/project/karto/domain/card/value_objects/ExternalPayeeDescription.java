package org.project.karto.domain.card.value_objects;

public record ExternalPayeeDescription(String value) {
    private static final int MAX_SIZE = 255;

    public ExternalPayeeDescription {
        if (value == null || value.isBlank())
            throw new IllegalArgumentException("ExternalPayeeDescription must not be null or blank");
        if (value.length() > MAX_SIZE) throw new IllegalArgumentException("Description is too long");

        value = value.strip();
    }

    @Override
    public String toString() {
        return value;
    }
}
