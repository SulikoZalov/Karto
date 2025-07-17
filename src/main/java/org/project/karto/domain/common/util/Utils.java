package org.project.karto.domain.common.util;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

public class Utils {

    private Utils() {}

    public static <T> T required(String fieldName, T value) {
        if (value == null)
            throw new IllegalDomainArgumentException(String.format("%s must not be null", fieldName));

        return value;
    }
}
