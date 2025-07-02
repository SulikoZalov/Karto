package org.project.karto.domain.common.util;

public class Utils {

    private Utils() {}

    public static void required(String fieldName, Object value) {
        if (value == null)
            throw new IllegalArgumentException(String.format("%s must not be null", fieldName));
    }
}
