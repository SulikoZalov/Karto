package org.project.karto.domain.common.value_objects;

public record KeyAndCounter(String key, int counter) {
    public static final int MAX_SIZE = 28;

    public KeyAndCounter {
        if (key == null) throw new IllegalArgumentException("Key can`t be null");
        if (key.length() > MAX_SIZE) throw new IllegalArgumentException("Key is too long");
        if (counter < 0) throw new IllegalArgumentException("Counter can`t be below zero");
    }
}