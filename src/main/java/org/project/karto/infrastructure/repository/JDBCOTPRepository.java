package org.project.karto.infrastructure.repository;

import com.hadzhy.jetquerious.jdbc.JetQuerious;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.user.entities.OTP;
import org.project.karto.domain.user.repository.OTPRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import static com.hadzhy.jetquerious.sql.QueryForge.*;
import static org.project.karto.infrastructure.repository.JDBCCompanyRepository.mapTransactionResult;

@ApplicationScoped
public class JDBCOTPRepository implements OTPRepository {

    private final JetQuerious jet;

    static final String SAVE_OTP = insert()
            .into("otp")
            .column("otp")
            .column("user_id")
            .column("is_confirmed")
            .column("creation_date")
            .column("expiration_date")
            .values()
            .build()
            .sql();

    static final String UPDATE_CONFIRMATION = update("otp")
            .set("is_confirmed = ?")
            .where("otp = ?")
            .build()
            .sql();

    static final String READ_OTP = select()
            .all()
            .from("otp")
            .where("otp = ?")
            .build()
            .sql();

    static final String OTP_BY_USER_ID = select()
            .all()
            .from("otp")
            .where("user_id = ?")
            .build()
            .sql();

    static final String REMOVE_OTP = delete()
            .from("otp")
            .where("otp = ?")
            .build()
            .sql();

    static final String IS_OTP_EXISTS = select()
            .count("*")
            .from("otp")
            .where("user_id = ?")
            .build()
            .sql();

    public JDBCOTPRepository() {
        jet = JetQuerious.instance();
    }

    @Override
    public Result<Integer, Throwable> save(OTP otp) {
        return mapTransactionResult(jet.write(SAVE_OTP,
                        otp.otp(),
                        otp.userID().toString(),
                        otp.isConfirmed(),
                        otp.creationDate(),
                        otp.expirationDate()));
    }

    @Override
    public Result<Integer, Throwable> updateConfirmation(OTP otp) {
        return mapTransactionResult(jet.write(UPDATE_CONFIRMATION, otp.isConfirmed(), otp.otp()));
    }

    @Override
    public Result<Integer, Throwable> remove(OTP otp) {
        return mapTransactionResult(jet.write(REMOVE_OTP, otp.otp()));
    }

    @Override
    public boolean contains(UUID userID) {
        return jet.readObjectOf(IS_OTP_EXISTS, Integer.class, userID)
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking email existence.");
                    return false;
                });
    }

    @Override
    public Result<OTP, Throwable> findBy(OTP otp) {
        return findBy(otp.otp());
    }

    @Override
    public Result<OTP, Throwable> findBy(String otp) {
        var result = jet.read(READ_OTP, this::otpMapper, otp);
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<OTP, Throwable> findBy(UUID userID) {
        var result = jet.read(OTP_BY_USER_ID, this::otpMapper, userID.toString());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    private OTP otpMapper(ResultSet rs) throws SQLException {
        return OTP.fromRepository(
                rs.getString("otp"),
                UUID.fromString(rs.getString("user_id")),
                rs.getBoolean("is_confirmed"),
                rs.getObject("creation_date", Timestamp.class).toLocalDateTime(),
                rs.getObject("expiration_date", Timestamp.class).toLocalDateTime());
    }
}
