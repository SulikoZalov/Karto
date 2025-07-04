package org.project.karto.domain.card.repositories;

import org.project.karto.domain.card.entities.Check;
import org.project.karto.domain.card.value_objects.BuyerID;
import org.project.karto.domain.card.value_objects.StoreID;
import org.project.karto.domain.common.containers.Result;

import java.util.List;
import java.util.UUID;

public interface CheckRepository {

    Result<Integer, Throwable> save(Check check);

    Result<Check, Throwable> findBy(UUID checkID);

    Result<List<Check>, Throwable> findBy(BuyerID buyerID);

    Result<List<Check>, Throwable> findBy(StoreID storeID);
}
