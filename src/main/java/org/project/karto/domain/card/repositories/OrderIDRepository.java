package org.project.karto.domain.card.repositories;

import org.project.karto.domain.common.containers.Result;

public interface OrderIDRepository {

    Result<Long, Throwable> next();
}
