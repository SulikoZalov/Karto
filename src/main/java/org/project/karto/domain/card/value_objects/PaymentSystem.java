package org.project.karto.domain.card.value_objects;

public record PaymentSystem(String value) {
    public PaymentSystem {
        if (value == null)
            throw new IllegalArgumentException("Payment system value can`t be null");
        if (value.isBlank())
            throw new IllegalArgumentException("Payment system value can`t be blank");
    }
}
