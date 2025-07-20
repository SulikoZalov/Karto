package org.project.karto.infrastructure.repository;

import com.hadzhy.jetquerious.jdbc.JetQuerious;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.common.value_objects.Email;
import org.project.karto.domain.common.value_objects.KeyAndCounter;
import org.project.karto.domain.common.value_objects.Phone;
import org.project.karto.domain.user.entities.User;
import org.project.karto.domain.user.repository.UserRepository;
import org.project.karto.domain.user.values_objects.CashbackStorage;
import org.project.karto.domain.user.values_objects.PersonalData;
import org.project.karto.domain.user.values_objects.RefreshToken;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import static com.hadzhy.jetquerious.sql.QueryForge.*;
import static org.project.karto.infrastructure.repository.JDBCCompanyRepository.mapTransactionResult;


@ApplicationScoped
public class JDBCUserRepository implements UserRepository {

    private final JetQuerious jet;

    static final String SAVE_USER = insert()
            .into("user_account")
            .column("id")
            .column("firstname")
            .column("surname")
            .column("phone")
            .column("email")
            .column("password")
            .column("birth_date")
            .column("is_verified")
            .column("is_2fa_enabled")
            .column("is_banned")
            .column("secret_key")
            .column("counter")
            .column("cashback_storage")
            .column("creation_date")
            .column("last_updated")
            .values()
            .build()
            .sql();

    static final String SAVE_REFRESH_TOKEN = insert()
            .into("refresh_token")
            .columns("user_id", "token")
            .values()
            .onConflict("user_id")
            .doUpdateSet("token = ?")
            .build()
            .sql();

    static final String UPDATE_PHONE = update("user_account")
            .set("phone = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_COUNTER = update("user_account")
            .set("counter = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_2FA = update("user_account")
            .set("is_2fa_enabled = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_VERIFICATION = update("user_account")
            .set("is_verified = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_CASHBACK_STORAGE = update("user_account")
            .set("cashback_storage = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_BAN = update("user_account")
            .set("is_banned")
            .where("id = ?")
            .build()
            .sql();

    static final String IS_EMAIL_EXISTS = select()
            .count("email")
            .from("user_account")
            .where("email = ?")
            .build()
            .sql();

    static final String IS_PHONE_EXISTS = select()
            .count("phone")
            .from("user_account")
            .where("phone = ?")
            .build()
            .sql();

    static final String USER_BY_ID = select()
            .all()
            .from("user_account")
            .where("id = ?")
            .build()
            .sql();

    static final String USER_BY_EMAIL = select()
            .all()
            .from("user_account")
            .where("email = ?")
            .build()
            .sql();

    static final String USER_BY_PHONE = select()
            .all()
            .from("user_account")
            .where("phone = ?")
            .build()
            .sql();

    static final String REFRESH_TOKEN = select()
            .all()
            .from("refresh_token")
            .where("token = ?")
            .build()
            .sql();

    public JDBCUserRepository() {
        jet = JetQuerious.instance();
    }

    @Override
    public Result<Integer, Throwable> save(User user) {
        PersonalData personalData = user.personalData();
        return mapTransactionResult(jet.write(SAVE_USER,
                        user.id().toString(),
                        personalData.firstname(),
                        personalData.surname(),
                        personalData.phone().orElse(null),
                        personalData.email(),
                        personalData.password().orElse(null),
                        personalData.birthDate(),
                        user.isVerified(),
                        user.is2FAEnabled(),
                        user.isBanned(),
                        user.keyAndCounter().key(),
                        user.keyAndCounter().counter(),
                        user.cashbackStorage(),
                        user.creationDate(),
                        user.lastUpdated()));
    }

    @Override
    public Result<Integer, Throwable> saveRefreshToken(RefreshToken refreshToken) {
        return mapTransactionResult(jet.write(SAVE_REFRESH_TOKEN,
                        refreshToken.userID().toString(),
                        refreshToken.refreshToken(),
                        refreshToken.refreshToken())
        );
    }

    @Override
    public Result<Integer, Throwable> updatePhone(User user) {
        return mapTransactionResult(jet.write(UPDATE_PHONE, user.personalData().phone().orElseThrow(), user.id().toString()));
    }

    @Override
    public Result<Integer, Throwable> updateCounter(User user) {
        return mapTransactionResult(jet.write(UPDATE_COUNTER, user.keyAndCounter().counter(), user.id().toString()));
    }

    @Override
    public Result<Integer, Throwable> update2FA(User user) {
        return mapTransactionResult(jet.write(UPDATE_2FA, user.is2FAEnabled(), user.id().toString()));
    }

    @Override
    public Result<Integer, Throwable> updateVerification(User user) {
        return mapTransactionResult(jet.write(UPDATE_VERIFICATION, user.isVerified(), user.id().toString()));
    }

    @Override
    public Result<Integer, Throwable> updateCashbackStorage(User user) {
        return mapTransactionResult(jet.write(UPDATE_CASHBACK_STORAGE, user.cashbackStorage(), user.id()));
    }

    @Override
    public Result<Integer, Throwable> updateBan(User user) {
        return mapTransactionResult(jet.write(UPDATE_BAN, user.isBanned(), user.id()));
    }

    @Override
    public boolean isEmailExists(Email email) {
        return jet.readObjectOf(IS_EMAIL_EXISTS, Integer.class, email.email())
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking email existence.");
                    return false;
                });
    }

    @Override
    public boolean isPhoneExists(Phone phone) {
        return jet.readObjectOf(IS_PHONE_EXISTS, Integer.class, phone.phoneNumber())
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking phone existence");
                    return false;
                });
    }

    @Override
    public Result<User, Throwable> findBy(UUID id) {
        var result = jet.read(USER_BY_ID, this::userMapper, id.toString());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<User, Throwable> findBy(Email email) {
        var result = jet.read(USER_BY_EMAIL, this::userMapper, email.email());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<User, Throwable> findBy(Phone phone) {
        var result = jet.read(USER_BY_PHONE, this::userMapper, phone.phoneNumber());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<RefreshToken, Throwable> findRefreshToken(String refreshToken) {
        var result = jet.read(REFRESH_TOKEN, this::refreshTokenMapper, refreshToken);
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    private RefreshToken refreshTokenMapper(ResultSet rs) throws SQLException {
        return new RefreshToken(UUID.fromString(rs.getString("user_id")), rs.getString("token"));
    }

    private User userMapper(ResultSet rs) throws SQLException {
        PersonalData personalData = new PersonalData(
                rs.getString("firstname"),
                rs.getString("surname"),
                rs.getString("phone"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getObject("birth_date", Timestamp.class)
                        .toLocalDateTime()
                        .toLocalDate());

        return User.fromRepository(
                UUID.fromString(rs.getString("id")),
                personalData,
                rs.getBoolean("is_verified"),
                rs.getBoolean("is_2fa_enabled"),
                rs.getBoolean("is_banned"),
                new KeyAndCounter(rs.getString("secret_key"), rs.getInt("counter")),
                new CashbackStorage(rs.getBigDecimal("cashback_storage")),
                rs.getObject("creation_date", Timestamp.class).toLocalDateTime(),
                rs.getObject("last_updated", Timestamp.class).toLocalDateTime());
    }
}
