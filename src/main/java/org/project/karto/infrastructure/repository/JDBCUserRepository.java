package org.project.karto.infrastructure.repository;

import com.hadzhy.jdbclight.jdbc.JDBC;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.user.entities.User;
import org.project.karto.domain.user.repository.UserRepository;
import org.project.karto.domain.user.values_objects.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import static com.hadzhy.jdbclight.sql.SQLBuilder.*;

@ApplicationScoped
public class JDBCUserRepository implements UserRepository {

    private final JDBC jdbc;

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
            .column("secret_key")
            .column("counter")
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

    static final String UPDATE_VERIFICATION = update("user_account")
            .set("is_verified = ?")
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
        jdbc = JDBC.instance();
    }

    @Override
    public void save(User user) {
        PersonalData personalData = user.personalData();
        jdbc.write(SAVE_USER,
                        user.id().toString(),
                        personalData.firstname(),
                        personalData.surname(),
                        personalData.phone().orElse(null),
                        personalData.email(),
                        personalData.password().orElse(null),
                        personalData.birthDate(),
                        user.isVerified(),
                        user.keyAndCounter().key(),
                        user.keyAndCounter().counter(),
                        user.creationDate(),
                        user.lastUpdated())
                .ifFailure(throwable -> Log.errorf("Error saving user: %s.", throwable.getMessage()));
    }

    @Override
    public void saveRefreshToken(RefreshToken refreshToken) {
        jdbc.write(SAVE_REFRESH_TOKEN,
                        refreshToken.userID().toString(),
                        refreshToken.refreshToken(),
                        refreshToken.refreshToken())
                .ifFailure(throwable -> Log.errorf("Error saving refresh token: %s.", throwable.getMessage()));
    }

    @Override
    public void updatePhone(User user) {
        jdbc.write(UPDATE_PHONE, user.personalData().phone(), user.id().toString())
                .ifFailure(throwable -> Log.errorf("Error update user phone: %s.", throwable.getMessage()));
    }

    @Override
    public void updateCounter(User user) {
        jdbc.write(UPDATE_COUNTER, user.keyAndCounter().counter(), user.id().toString())
                .ifFailure(throwable -> Log.errorf("Error update user phone: %s.", throwable.getMessage()));
    }

    @Override
    public void updateVerification(User user) {
        jdbc.write(UPDATE_VERIFICATION, user.isVerified(), user.id().toString())
                .ifFailure(throwable -> Log.errorf("Error update verification: %s.", throwable.getMessage()));
    }

    @Override
    public boolean isEmailExists(Email email) {
        return jdbc.readObjectOf(IS_EMAIL_EXISTS, Integer.class, email.email())
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking email existence.");
                    return false;
                });
    }

    @Override
    public boolean isPhoneExists(Phone phone) {
        return jdbc.readObjectOf(IS_PHONE_EXISTS, Integer.class, phone.phoneNumber())
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking phone existence");
                    return false;
                });
    }

    @Override
    public Result<User, Throwable> findBy(UUID id) {
        var result = jdbc.read(USER_BY_ID, this::userMapper, id.toString());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<User, Throwable> findBy(Email email) {
        var result = jdbc.read(USER_BY_EMAIL, this::userMapper, email.email());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<User, Throwable> findBy(Phone phone) {
        var result = jdbc.read(USER_BY_PHONE, this::userMapper, phone.phoneNumber());
        return new Result<>(result.value(), result.throwable(), result.success());
    }

    @Override
    public Result<RefreshToken, Throwable> findRefreshToken(String refreshToken) {
        var result = jdbc.read(REFRESH_TOKEN, this::refreshTokenMapper, refreshToken);
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
                new KeyAndCounter(rs.getString("secret_key"), rs.getInt("counter")),
                rs.getObject("creation_date", Timestamp.class).toLocalDateTime(),
                rs.getObject("last_updated", Timestamp.class).toLocalDateTime());
    }
}
