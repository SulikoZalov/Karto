package org.project.karto.domain.card.value_objects;

import static org.project.karto.domain.common.util.Utils.required;

public record Currency(String code) {

    public Currency {
        required("currency code", code);
        String upperCode = code.toUpperCase();
        try {
            java.util.Currency.getInstance(upperCode);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid currency code: " + code, e);
        }

        code = upperCode;
    }

    public static Currency getInstance(String code) {
        return new Currency(code);
    }

    @Override
    public String toString() {
        return code;
    }
}
