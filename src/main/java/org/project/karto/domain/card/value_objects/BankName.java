package org.project.karto.domain.card.value_objects;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

public record BankName(String value) {

    public BankName {
        if (value == null) throw new IllegalDomainArgumentException("Bank name must not be null");
        if (value.isBlank()) throw new IllegalDomainArgumentException("Bank name must not be blank");
        if (value.length() > 255) throw new IllegalDomainArgumentException("Bank name must be 255 characters or fewer");
    }

    @Override
    public String toString() {
        return value;
    }
}
