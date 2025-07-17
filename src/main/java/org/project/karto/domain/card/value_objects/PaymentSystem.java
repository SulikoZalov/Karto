package org.project.karto.domain.card.value_objects;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

public record PaymentSystem(String value) {
    private static final int MAX_SIZE = 255;

    public PaymentSystem {
        if (value == null) throw new IllegalDomainArgumentException("Payment system value can`t be null");
        if (value.isBlank()) throw new IllegalDomainArgumentException("Payment system value can`t be blank");
        if (value.length() > MAX_SIZE)
            throw new IllegalDomainArgumentException("Payment system name can`t be longer than 255 characters");
    }

    @Override
    public String toString() {
        return value;
    }
}
