package org.project.karto.infrastructure.repository;

import com.hadzhy.jetquerious.jdbc.JetQuerious;
import jakarta.enterprise.context.ApplicationScoped;
import org.project.karto.domain.card.entities.Check;
import org.project.karto.domain.card.enumerations.CheckType;
import org.project.karto.domain.card.enumerations.PaymentType;
import org.project.karto.domain.card.repositories.CheckRepository;
import org.project.karto.domain.card.value_objects.*;
import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.common.value_objects.Amount;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static com.hadzhy.jetquerious.sql.QueryForge.insert;
import static com.hadzhy.jetquerious.sql.QueryForge.select;
import static org.project.karto.infrastructure.repository.JDBCCompanyRepository.mapTransactionResult;

@ApplicationScoped
public class JDBCCheckRepository implements CheckRepository {

    private final JetQuerious jet;

    static final String SAVE_CHECK = insert().into("chck")
            .columns("id",
                    "order_id",
                    "buyer_id",
                    "store_id",
                    "card_id",
                    "total_amount",
                    "currency",
                    "payment_type",
                    "internal_fee",
                    "external_fee",
                    "payment_system",
                    "description",
                    "bank_name",
                    "creation_date",
                    "check_type")
            .values()
            .build()
            .sql();

    static final String FIND_BY_CHECK_ID = select()
            .all()
            .from("chck")
            .where("id = ?")
            .build()
            .sql();

    static final String FIND_BY_BUYER_ID = select()
            .all()
            .from("chck")
            .where("buyer_id = ?")
            .build()
            .sql();

    static final String FIND_BY_STORE_ID = select()
            .all()
            .from("chck")
            .where("store_id = ?")
            .build()
            .sql();

    JDBCCheckRepository() {
        this.jet = JetQuerious.instance();
    }

    @Override
    public Result<Integer, Throwable> save(Check check) {
        return mapTransactionResult(jet.write(SAVE_CHECK,
                check.id(),
                check.orderID(),
                check.buyerID(),
                check.storeID().orElse(null),
                check.cardID().orElse(null),
                check.totalAmount(),
                check.currency(),
                check.paymentType(),
                check.internalFee(),
                check.externalFee(),
                check.paymentSystem(),
                check.description(),
                check.bankName(),
                check.creationDate(),
                check.checkType()));
    }

    @Override
    public Result<Check, Throwable> findBy(UUID checkID) {
        var result = jet.read(FIND_BY_CHECK_ID, this::mapCheck, checkID.toString());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<List<Check>, Throwable> findBy(BuyerID buyerID) {
        var result = jet.readListOf(FIND_BY_BUYER_ID, this::mapCheck, buyerID.value().toString());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<List<Check>, Throwable> findBy(StoreID storeID) {
        var result = jet.readListOf(FIND_BY_STORE_ID, this::mapCheck, storeID.value().toString());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    private Check mapCheck(ResultSet rs) throws SQLException {
        String storeID = rs.getString("store_id");
        String cardID = rs.getString("card_id");

        return Check.fromRepository(
                UUID.fromString(rs.getString("id")),
                rs.getLong("order_id"),
                BuyerID.fromString(rs.getString("buyer_id")),
                storeID != null ? StoreID.fromString(storeID) : null,
                CardID.fromString(cardID),
                new Amount(rs.getBigDecimal("total_amount")),
                new Currency(rs.getString("currency")),
                PaymentType.valueOf(rs.getString("payment_type")),
                new InternalFeeAmount(rs.getBigDecimal("internal_fee")),
                new ExternalFeeAmount(rs.getBigDecimal("external_fee")),
                new PaymentSystem(rs.getString("payment_system")),
                new PayeeDescription(rs.getString("description")),
                new BankName(rs.getString("bank_name")),
                rs.getTimestamp("creation_date").toLocalDateTime(),
                CheckType.valueOf(rs.getString("check_type")));
    }
}
