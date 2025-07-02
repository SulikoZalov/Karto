package org.project.karto.infrastructure.repository;

import com.hadzhy.jetquerious.jdbc.JetQuerious;
import com.hadzhy.jetquerious.sql.QueryForge;
import jakarta.enterprise.context.ApplicationScoped;
import org.project.karto.domain.card.entities.PaymentIntent;
import org.project.karto.domain.card.enumerations.PurchaseStatus;
import org.project.karto.domain.card.repositories.PaymentIntentRepository;
import org.project.karto.domain.card.value_objects.BuyerID;
import org.project.karto.domain.card.value_objects.CardID;
import org.project.karto.domain.card.value_objects.ExternalPayeeDescription;
import org.project.karto.domain.card.value_objects.StoreID;
import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.common.value_objects.Amount;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import static com.hadzhy.jetquerious.sql.QueryForge.insert;
import static com.hadzhy.jetquerious.sql.QueryForge.select;
import static org.project.karto.infrastructure.repository.JDBCCompanyRepository.mapTransactionResult;

@ApplicationScoped
public class JDBCPaymentIntentRepository implements PaymentIntentRepository {

    private final JetQuerious jet;

    static final String SAVE_PAYMENT_INTENT = insert()
            .into("payment_intent")
            .column("id")
            .column("buyer_id")
            .column("card_id")
            .column("store_id")
            .column("order_id")
            .column("total_amount")
            .column("creation_date")
            .column("result_date")
            .column("status")
            .column("is_confirmed")
            .column("description")
            .values()
            .build()
            .sql();

    static final String UPDATE_STATUS = QueryForge.update("payment_intent")
            .set("""
                result_date = ?,
                status = ?,
                description = ?
            """)
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_CONFIRMATION = QueryForge.update("payment_intent")
            .set("is_confirmed = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String FIND_BY_ID = select()
            .all()
            .from("payment_intent")
            .where("id = ?")
            .build()
            .sql();

    static final String FIND_BY_ORDER_ID = select()
            .all()
            .from("payment_intent")
            .where("order_id = ?")
            .build()
            .sql();

    JDBCPaymentIntentRepository() {
        this.jet = JetQuerious.instance();
    }

    @Override
    public Result<Integer, Throwable> save(PaymentIntent paymentIntent) {
        return mapTransactionResult(jet.write(SAVE_PAYMENT_INTENT,
                paymentIntent.id(),
                paymentIntent.buyerID(),
                paymentIntent.cardID(),
                paymentIntent.storeID().orElse(null),
                paymentIntent.orderID(),
                paymentIntent.totalAmount(),
                paymentIntent.creationDate(),
                paymentIntent.resultDate().orElse(null),
                paymentIntent.status(),
                paymentIntent.isConfirmed(),
                paymentIntent.paymentDescription()
        ));
    }

    @Override
    public Result<Integer, Throwable> update(PaymentIntent paymentIntent) {
        return mapTransactionResult(jet.write(UPDATE_STATUS,
                paymentIntent.resultDate().orElse(null),
                paymentIntent.status(),
                paymentIntent.paymentDescription(),
                paymentIntent.id()
        ));
    }

    @Override
    public Result<Integer, Throwable> updateConfirmation(PaymentIntent paymentIntent) {
        return mapTransactionResult(jet.write(UPDATE_CONFIRMATION, paymentIntent.isConfirmed(), paymentIntent.id()));
    }

    @Override
    public Result<PaymentIntent, Throwable> findBy(UUID id) {
        return mapResult(jet.read(FIND_BY_ID, this::mapPaymentIntent, id));
    }

    @Override
    public Result<PaymentIntent, Throwable> findBy(long orderID) {
        return mapResult(jet.read(FIND_BY_ORDER_ID, this::mapPaymentIntent, orderID));
    }

    private PaymentIntent mapPaymentIntent(ResultSet rs) throws SQLException {
        String storeID = rs.getString("store_id");
        Timestamp resultDate = rs.getTimestamp("result_date");
        String description = rs.getString("description");

        return PaymentIntent.fromRepository(
                UUID.fromString(rs.getString("id")),
                BuyerID.fromString(rs.getString("buyer_id")),
                CardID.fromString(rs.getString("card_id")),
                storeID == null ? null : StoreID.fromString(storeID),
                rs.getLong("order_id"),
                new Amount(rs.getBigDecimal("total_amount")),
                rs.getTimestamp("creation_date").toLocalDateTime(),
                resultDate == null ? null : resultDate.toLocalDateTime(),
                PurchaseStatus.valueOf(rs.getString("status")),
                rs.getBoolean("is_confirmed"),
                description == null ? null : new ExternalPayeeDescription(description)
        );
    }

    private static Result<PaymentIntent, Throwable> mapResult(com.hadzhy.jetquerious.util.Result<PaymentIntent, Throwable> res) {
        return new Result<>(res.value(), res.throwable(), res.success());
    }
}
