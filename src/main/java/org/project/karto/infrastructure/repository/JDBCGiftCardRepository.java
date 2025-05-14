package org.project.karto.infrastructure.repository;

import com.hadzhy.jdbclight.jdbc.JDBC;
import com.hadzhy.jdbclight.sql.SQLBuilder;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.project.karto.domain.card.entities.GiftCard;
import org.project.karto.domain.card.enumerations.GiftCardStatus;
import org.project.karto.domain.card.enumerations.Store;
import org.project.karto.domain.card.repositories.GiftCardRepository;
import org.project.karto.domain.card.value_objects.*;
import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.common.value_objects.KeyAndCounter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import static com.hadzhy.jdbclight.sql.SQLBuilder.*;

@ApplicationScoped
public class JDBCGiftCardRepository implements GiftCardRepository {

    private final JDBC jdbc;

    static final String SAVE_GIFT_CARD = insert()
            .into("gift_card")
            .columns("id",
                    "buyer_id",
                    "owner_id",
                    "store_id",
                    "gift_card_status",
                    "balance",
                    "count_of_uses",
                    "is_verified",
                    "secret_key",
                    "counter",
                    "creation_date",
                    "expiration_date")
            .values()
            .build()
            .sql();

    static final String UPDATE_GIFT_CARD = SQLBuilder.update("gift_card")
            .set("""
                  gift_card_status = ?,
                  balance = ?,
                  count_of_uses = ?,
                  is_verified = ?,
                  secret_key = ?,
                  counter = ?,
                  expiration_date = ?
                  """)
            .where("id = ?")
            .build()
            .sql();

    static final String FIND_BY_CARD_ID = select()
            .all()
            .from("gift_card")
            .where("id = ?")
            .build()
            .sql();

    static final String FIND_BY_BUYER_ID = select()
            .all()
            .from("gift_card")
            .where("buyer_id = ?")
            .build()
            .sql();

    static final String FIND_BY_OWNER_ID = select()
            .all()
            .from("gift_card")
            .where("owner_id = ?")
            .build()
            .sql();

    static final String FIND_BY_STORE_ID = select()
            .all()
            .from("gift_card")
            .where("store_id = ?")
            .build()
            .sql();

    JDBCGiftCardRepository() {
        this.jdbc = JDBC.instance();
    }

    @Override
    public void save(GiftCard giftCard) {
        jdbc.write(SAVE_GIFT_CARD,
                giftCard.id().value().toString(),
                giftCard.buyerID().value().toString(),
                giftCard.ownerID() != null ? giftCard.ownerID().value().toString() : null,
                giftCard.store().name(),
                giftCard.giftCardStatus().name(),
                giftCard.balance().value(),
                giftCard.countOfUses(),
                giftCard.isVerified(),
                giftCard.keyAndCounter().key(),
                giftCard.keyAndCounter().counter(),
                Timestamp.valueOf(giftCard.creationDate()),
                Timestamp.valueOf(giftCard.expirationDate())
        ).ifFailure(throwable ->
                Log.errorf("Error saving gift card: %s", throwable.getMessage()));
    }

    @Override
    public void update(GiftCard giftCard) {
        jdbc.write(UPDATE_GIFT_CARD,
                giftCard.giftCardStatus().name(),
                giftCard.balance().value(),
                giftCard.countOfUses(),
                giftCard.isVerified(),
                giftCard.keyAndCounter().key(),
                giftCard.keyAndCounter().counter(),
                Timestamp.valueOf(giftCard.expirationDate()),
                giftCard.id().toString()
        ).ifFailure(throwable ->
                Log.errorf("Error updating gift card: %s", throwable.getMessage()));
    }

    @Override
    public Result<GiftCard, Throwable> findBy(CardID cardID) {
        var result = jdbc.read(FIND_BY_CARD_ID, this::mapGiftCard, cardID.value().toString());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<List<GiftCard>, Throwable> findBy(BuyerID buyerID) {
        var result = jdbc.readListOf(FIND_BY_BUYER_ID, this::mapGiftCard, buyerID.value().toString());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<List<GiftCard>, Throwable> findBy(OwnerID ownerID) {
        var result = jdbc.readListOf(FIND_BY_OWNER_ID, this::mapGiftCard, ownerID.value().toString());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<List<GiftCard>, Throwable> findBy(Store store) {
        var result = jdbc.readListOf(FIND_BY_STORE_ID, this::mapGiftCard, store.name());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    private GiftCard mapGiftCard(ResultSet rs) throws SQLException {
        String ownerId = rs.getString("owner_id");
        return GiftCard.fromRepository(
                CardID.fromString(rs.getString("id")),
                BuyerID.fromString(rs.getString("buyer_id")),
                ownerId != null ? OwnerID.fromString(ownerId) : null,
                Store.valueOf(rs.getString("store_id")),
                GiftCardStatus.valueOf(rs.getString("gift_card_status")),
                new Balance(rs.getBigDecimal("balance")),
                rs.getInt("count_of_uses"),
                rs.getBoolean("is_verified"),
                new KeyAndCounter(
                        rs.getString("secret_key"),
                        rs.getInt("counter")
                ),
                rs.getTimestamp("creation_date").toLocalDateTime(),
                rs.getTimestamp("expiration_date").toLocalDateTime()
        );
    }
}
