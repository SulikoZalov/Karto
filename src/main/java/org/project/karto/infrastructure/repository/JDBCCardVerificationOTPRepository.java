package org.project.karto.infrastructure.repository;

import com.hadzhy.jdbclight.jdbc.JDBC;
import com.hadzhy.jdbclight.sql.SQLBuilder;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.project.karto.domain.card.entities.CardVerificationOTP;
import org.project.karto.domain.card.repositories.CardVerificationOTPRepository;
import org.project.karto.domain.card.value_objects.CardID;
import org.project.karto.domain.card.value_objects.OwnerID;
import org.project.karto.domain.common.containers.Result;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static com.hadzhy.jdbclight.sql.SQLBuilder.*;

@ApplicationScoped
public class JDBCCardVerificationOTPRepository implements CardVerificationOTPRepository {

    private final JDBC jdbc;

    static final String SAVE_OTP = insert()
            .into("gift_card_otp")
            .columns("otp", "card_id", "is_confirmed", "creation_date", "expiration_date")
            .values()
            .build()
            .sql();

    static final String UPDATE_CONFIRMATION = SQLBuilder.update("gift_card_otp")
            .set("is_confirmed = ?")
            .where("otp = ?")
            .build()
            .sql();

    static final String DELETE_OTP = delete()
            .from("gift_card_otp")
            .where("otp = ?")
            .build()
            .sql();

    static final String FIND_BY_OTP = select()
            .all()
            .from("gift_card_otp")
            .where("otp = ?")
            .build()
            .sql();

    static final String FIND_BY_CARD_ID = select()
            .all()
            .from("gift_card_otp")
            .where("card_id = ?")
            .build()
            .sql();

    static final String FIND_BY_OWNER_ID = select()
            .all()
            .from("gift_card_otp AS o")
            .joinAs( "gift_card", "c", "o.card_id = c.id")
            .where("c.owner_id = ?")
            .build()
            .sql();

    JDBCCardVerificationOTPRepository() {
        this.jdbc = JDBC.instance();
    }

    @Override
    public void save(CardVerificationOTP otp) {
        jdbc.write(SAVE_OTP,
                        otp.otp(),
                        otp.cardID().value().toString(),
                        otp.isConfirmed(),
                        Timestamp.valueOf(otp.creationDate()),
                        Timestamp.valueOf(otp.expirationDate()))
                .ifFailure(throwable ->
                        Log.errorf("Error saving gift card OTP: %s", throwable.getMessage()));
    }

    @Override
    public void update(CardVerificationOTP otp) {
        jdbc.write(UPDATE_CONFIRMATION,
                        otp.isConfirmed(),
                        otp.otp())
                .ifFailure(throwable ->
                        Log.errorf("Error updating OTP confirmation: %s", throwable.getMessage()));
    }

    @Override
    public void remove(CardVerificationOTP otp) {
        jdbc.write(DELETE_OTP, otp.otp())
                .ifFailure(throwable ->
                        Log.errorf("Error deleting gift card OTP: %s", otp.otp()));
    }

    @Override
    public Result<CardVerificationOTP, Throwable> findBy(CardVerificationOTP otp) {
        return findBy(otp.otp());
    }

    @Override
    public Result<CardVerificationOTP, Throwable> findBy(String otp) {
        var result = jdbc.read(FIND_BY_OTP, this::mapOtp, otp);
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<CardVerificationOTP, Throwable> findBy(OwnerID ownerID) {
        var result = jdbc.read(FIND_BY_OWNER_ID, this::mapOtp, ownerID.value().toString());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<CardVerificationOTP, Throwable> findBy(CardID cardID) {
        var result = jdbc.read(FIND_BY_CARD_ID, this::mapOtp, cardID.value().toString());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    private CardVerificationOTP mapOtp(ResultSet rs) throws SQLException {
        return CardVerificationOTP.fromRepository(
                rs.getString("otp"),
                CardID.fromString(rs.getString("card_id")),
                rs.getBoolean("is_confirmed"),
                convertTimestamp(rs.getTimestamp("creation_date")),
                convertTimestamp(rs.getTimestamp("expiration_date"))
        );
    }

    private LocalDateTime convertTimestamp(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}