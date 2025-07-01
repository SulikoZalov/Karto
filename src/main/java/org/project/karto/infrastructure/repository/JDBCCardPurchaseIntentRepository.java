package org.project.karto.infrastructure.repository;

import com.hadzhy.jetquerious.jdbc.JetQuerious;
import com.hadzhy.jetquerious.sql.QueryForge;
import jakarta.enterprise.context.ApplicationScoped;
import org.project.karto.domain.card.entities.CardPurchaseIntent;
import org.project.karto.domain.card.enumerations.PurchaseStatus;
import org.project.karto.domain.card.repositories.CardPurchaseIntentRepository;
import org.project.karto.domain.card.value_objects.BuyerID;
import org.project.karto.domain.card.value_objects.Fee;
import org.project.karto.domain.card.value_objects.StoreID;
import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.common.value_objects.Amount;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import static com.hadzhy.jetquerious.sql.QueryForge.insert;
import static com.hadzhy.jetquerious.sql.QueryForge.select;
import static org.project.karto.infrastructure.repository.JDBCCompanyRepository.mapTransactionResult;

@ApplicationScoped
public class JDBCCardPurchaseIntentRepository implements CardPurchaseIntentRepository {

    private final JetQuerious jet;

    static final String SAVE_CARD_PURCHASE_INTENT = insert()
            .into("card_purchase_intent")
            .column("id")
            .column("buyer_id")
            .column("store_id")
            .column("order_id")
            .column("total_payed_amount")
            .column("creation_date")
            .column("result_date")
            .column("status")
            .column("removed_fee")
            .values()
            .build()
            .sql();

    static final String UPDATE_PURCHASE_INTENT = QueryForge.update("card_purchase_intent")
            .set("""
                result_date = ?,
                status = ?,
                removed_fee = ?
             """)
            .where("id = ?")
            .build()
            .sql();

    static final String FIND_BY_ID = select()
            .all()
            .from("card_purchase_intent")
            .where("id = ?")
            .build()
            .sql();

    static final String FIND_BY_BUYER_ID = select()
            .all()
            .from("card_purchase_intent")
            .where("buyer_id = ?")
            .build()
            .sql();

    static final String FIND_BY_ORDER_ID = select()
            .all()
            .from("card_purchase_intent")
            .where("order_id = ?")
            .build()
            .sql();

    JDBCCardPurchaseIntentRepository() {
        this.jet = JetQuerious.instance();
    }

    @Override
    public Result<Integer, Throwable> save(CardPurchaseIntent purchaseIntent) {
        return mapTransactionResult(jet.write(SAVE_CARD_PURCHASE_INTENT,
                purchaseIntent.id(),
                purchaseIntent.buyerID(),
                purchaseIntent.storeID().orElse(null),
                purchaseIntent.orderID(),
                purchaseIntent.totalPayedAmount(),
                purchaseIntent.creationDate(),
                purchaseIntent.resultDate().orElse(null),
                purchaseIntent.status(),
                purchaseIntent.removedFee().orElse(null)));
    }

    @Override
    public Result<Integer, Throwable> update(CardPurchaseIntent purchaseIntent) {
        return mapTransactionResult(jet.write(UPDATE_PURCHASE_INTENT,
                purchaseIntent.resultDate().orElse(null),
                purchaseIntent.status(),
                purchaseIntent.removedFee().orElse(null),
                purchaseIntent.id()));
    }

    @Override
    public Result<CardPurchaseIntent, Throwable> findBy(UUID id) {
        return mapResult(jet.read(FIND_BY_ID, this::mapCardPurchaseIntent, id));
    }

    @Override
    public Result<CardPurchaseIntent, Throwable> findBy(BuyerID buyerID) {
        return mapResult(jet.read(FIND_BY_BUYER_ID, this::mapCardPurchaseIntent, buyerID));
    }

    @Override
    public Result<CardPurchaseIntent, Throwable> findBy(long orderID) {
        return mapResult(jet.read(FIND_BY_ORDER_ID, this::mapCardPurchaseIntent, orderID));
    }

    private CardPurchaseIntent mapCardPurchaseIntent(ResultSet rs) throws SQLException {
        Timestamp resultDate = rs.getTimestamp("result_date");
        BigDecimal removedFee = rs.getBigDecimal("removed_fee");
        String storeID = rs.getString("store_id");

        return CardPurchaseIntent.fromRepository(
                UUID.fromString(rs.getString("id")),
                BuyerID.fromString(rs.getString("buyer_id")),
                storeID == null ? null : StoreID.fromString(storeID),
                rs.getLong("order_id"),
                new Amount(rs.getBigDecimal("total_payed_amount")),
                rs.getTimestamp("creation_date").toLocalDateTime(),
                resultDate == null ? null : resultDate.toLocalDateTime(),
                PurchaseStatus.valueOf(rs.getString("status")),
                removedFee == null ? null : new Fee(removedFee)
        );
    }

    private static Result<CardPurchaseIntent, Throwable> mapResult(
            com.hadzhy.jetquerious.util.Result<CardPurchaseIntent, Throwable> result) {
        return new Result<>(result.value(), result.throwable(), result.success());
    }
}
