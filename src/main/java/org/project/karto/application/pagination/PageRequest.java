package org.project.karto.application.pagination;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

public record PageRequest(int limit, int offset) {
    public PageRequest {
        if (offset < 0)
            throw new IllegalDomainArgumentException("Offset cannot be negative");
        if (limit <= 0)
            throw new IllegalDomainArgumentException("Limit must be positive");
    }
}
