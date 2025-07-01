package org.project.karto.infrastructure.repository;

import com.hadzhy.jetquerious.jdbc.JetQuerious;
import jakarta.enterprise.context.ApplicationScoped;
import org.project.karto.domain.card.repositories.OrderIDRepository;
import org.project.karto.domain.common.containers.Result;

@ApplicationScoped
public class JDBCOrderIDRepository implements OrderIDRepository {

    private final JetQuerious jet;

    static final String NEXT = "SELECT nextval('card_purchase_intent_order_id_seq')";

    JDBCOrderIDRepository() {
        jet = JetQuerious.instance();
    }

    @Override
    public Result<Long, Throwable> next() {
        var result = jet.readObjectOf(NEXT, Long.class);
        return new Result<>(result.value(), result.throwable(), result.success());
    }
}
