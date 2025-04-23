package org.project.karto.infrastructure.repository;

import com.hadzhy.jdbclight.jdbc.JDBC;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.user.repository.OTPRepository;
import org.project.karto.domain.user.entities.OTP;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import static com.hadzhy.jdbclight.sql.SQLBuilder.*;

@ApplicationScoped
public class JDBCOTPRepository implements OTPRepository {

    private final JDBC jdbc;

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

    public JDBCOTPRepository() {
        jdbc = JDBC.instance();
    }

    @Override
    public void save(OTP otp) {
        jdbc.write(SAVE_OTP,
                        otp.otp(),
                        otp.userID().toString(),
                        otp.isConfirmed(),
                        otp.creationDate(),
                        otp.expirationDate())
                .ifFailure(throwable -> Log.errorf("Error saving otp: %s.", throwable.getMessage()));
    }

    @Override
    public void updateConfirmation(OTP otp) {
        jdbc.write(UPDATE_CONFIRMATION, otp.isConfirmed(), otp.otp())
                .ifFailure(throwable -> Log.errorf("Error update otp confirmation: %s.", throwable.getMessage()));
    }

    @Override
    public void remove(OTP otp) {
        jdbc.write(REMOVE_OTP, otp.otp())
                .ifFailure(throwable -> Log.errorf("Error deleting otp: %s.", otp.otp()));
    }

    @Override
    public Result<OTP, Throwable> findBy(OTP otp) {
        return findBy(otp.otp());
    }

    @Override
    public Result<OTP, Throwable> findBy(String otp) {
        var result = jdbc.read(READ_OTP, this::otpMapper, otp);
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<OTP, Throwable> findBy(UUID userID) {
        var result = jdbc.read(OTP_BY_USER_ID, this::otpMapper, userID.toString());
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
