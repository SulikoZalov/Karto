package org.project.karto.application.pagination;

public record PageRequest(int offset, int limit) {
    public PageRequest {
        if (offset < 0)
            throw new IllegalArgumentException("Offset cannot be negative");
        if (limit <= 0)
            throw new IllegalArgumentException("Limit must be positive");
    }
}
