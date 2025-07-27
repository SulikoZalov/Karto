package org.project.karto.application.pagination;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

public record PageRequest(int limit, int offset) {
    public PageRequest {
        if (offset < 0)
            throw new IllegalDomainArgumentException("Offset cannot be negative");
        if (limit <= 0)
            throw new IllegalDomainArgumentException("Limit must be positive");

        limit = buildLimit(limit);
        offset = buildOffSet(limit, offset);
    }

    static int buildLimit(Integer pageSize) {
        int limit;
        if (pageSize > 0 && pageSize <= 25) {
            limit = pageSize;
        } else {
            limit = 10;
        }
        return limit;
    }

    static int buildOffSet(Integer limit, Integer pageNumber) {
        int offSet;
        if (limit > 0 && pageNumber > 0) {
            offSet = (pageNumber - 1) * limit;
        } else {
            offSet = 0;
        }
        return offSet;
    }
}
