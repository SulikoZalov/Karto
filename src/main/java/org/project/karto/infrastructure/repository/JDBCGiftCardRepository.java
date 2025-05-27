package org.project.karto.infrastructure.repository;

import com.hadzhy.jetquerious.jdbc.JetQuerious;
import com.hadzhy.jetquerious.sql.QueryForge;
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
import java.util.List;

import static com.hadzhy.jetquerious.sql.QueryForge.insert;
import static com.hadzhy.jetquerious.sql.QueryForge.select;

@ApplicationScoped
public class JDBCGiftCardRepository implements GiftCardRepository {

    private final JetQuerious jet;

    static final String SAVE_GIFT_CARD = insert()
            .into("gift_card")
            .columns("id",
                    "pan",
                    "buyer_id",
                    "owner_id",
                    "store",
                    "gift_card_status",
                    "balance",
                    "count_of_uses",
                    "max_count_of_uses",
                    "secret_key",
                    "counter",
                    "creation_date",
                    "expiration_date",
                    "last_usage")
            .values()
            .build()
            .sql();

    static final String UPDATE_GIFT_CARD = QueryForge.update("gift_card")
            .set("""
                  gift_card_status = ?,
                  balance = ?,
                  count_of_uses = ?,
                  secret_key = ?,
                  counter = ?,
                  last_usage = ?
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
            .where("store = ?")
            .build()
            .sql();

    JDBCGiftCardRepository() {
        this.jet = JetQuerious.instance();
    }

    @Override
    public void save(GiftCard giftCard) {
        jet.write(SAVE_GIFT_CARD,
                giftCard.id().value().toString(),
                giftCard.pan(),
                giftCard.buyerID().value().toString(),
                giftCard.ownerID() != null ? giftCard.ownerID().value().toString() : null,
                giftCard.store().name(),
                giftCard.giftCardStatus().name(),
                giftCard.balance().value(),
                giftCard.countOfUses(),
                giftCard.maxCountOfUses(),
                giftCard.keyAndCounter().key(),
                giftCard.keyAndCounter().counter(),
                giftCard.creationDate(),
                giftCard.expirationDate(),
                giftCard.lastUsage()
        ).ifFailure(throwable ->
                Log.errorf("Error saving gift card: %s", throwable.getMessage()));
    }

    @Override
    public void update(GiftCard giftCard) {
        jet.write(UPDATE_GIFT_CARD,
                giftCard.giftCardStatus().name(),
                giftCard.balance().value(),
                giftCard.countOfUses(),
                giftCard.keyAndCounter().key(),
                giftCard.keyAndCounter().counter(),
                giftCard.lastUsage(),
                giftCard.id().toString()
        ).ifFailure(throwable ->
                Log.errorf("Error updating gift card: %s", throwable.getMessage()));
    }

    @Override
    public Result<GiftCard, Throwable> findBy(CardID cardID) {
        var result = jet.read(FIND_BY_CARD_ID, this::mapGiftCard, cardID.value().toString());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<List<GiftCard>, Throwable> findBy(BuyerID buyerID) {
        var result = jet.readListOf(FIND_BY_BUYER_ID, this::mapGiftCard, buyerID.value().toString());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<List<GiftCard>, Throwable> findBy(OwnerID ownerID) {
        var result = jet.readListOf(FIND_BY_OWNER_ID, this::mapGiftCard, ownerID.value().toString());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<List<GiftCard>, Throwable> findBy(Store store) {
        var result = jet.readListOf(FIND_BY_STORE_ID, this::mapGiftCard, store.name());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    private GiftCard mapGiftCard(ResultSet rs) throws SQLException {
        String ownerId = rs.getString("owner_id");
        return GiftCard.fromRepository(
                CardID.fromString(rs.getString("id")),
                new PAN(rs.getString("pan")),
                BuyerID.fromString(rs.getString("buyer_id")),
                ownerId != null ? OwnerID.fromString(ownerId) : null,
                Store.valueOf(rs.getString("store")),
                GiftCardStatus.valueOf(rs.getString("gift_card_status")),
                new Balance(rs.getBigDecimal("balance")),
                rs.getInt("count_of_uses"),
                rs.getInt("max_count_of_uses"),
                new KeyAndCounter(
                        rs.getString("secret_key"),
                        rs.getInt("counter")
                ),
                rs.getTimestamp("creation_date").toLocalDateTime(),
                rs.getTimestamp("expiration_date").toLocalDateTime(),
                rs.getTimestamp("last_usage").toLocalDateTime()
        );
    }
}
