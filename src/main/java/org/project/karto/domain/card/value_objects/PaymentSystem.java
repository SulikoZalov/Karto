package org.project.karto.domain.card.value_objects;

public record PaymentSystem(String value) {
    private static final int MAX_SIZE = 255;

    public PaymentSystem {
        if (value == null) throw new IllegalArgumentException("Payment system value can`t be null");
        if (value.isBlank()) throw new IllegalArgumentException("Payment system value can`t be blank");
        if (value.length() > MAX_SIZE)
            throw new IllegalArgumentException("Payment system name can`t be longer than 255 characters");
    }

    @Override
    public String toString() {
        return value;
    }
}
