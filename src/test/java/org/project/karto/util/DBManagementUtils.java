package org.project.karto.util;

import com.hadzhy.jdbclight.jdbc.JDBC;
import jakarta.inject.Singleton;
import org.project.karto.application.dto.RegistrationForm;
import org.project.karto.domain.user.entities.OTP;
import org.project.karto.domain.user.entities.User;
import org.project.karto.domain.user.repository.OTPRepository;
import org.project.karto.domain.user.repository.UserRepository;
import org.project.karto.domain.user.values_objects.Email;
import org.project.karto.domain.user.values_objects.PersonalData;
import org.project.karto.infrastructure.security.HOTPGenerator;
import org.project.karto.infrastructure.security.PasswordEncoder;

import java.util.Objects;

import static com.hadzhy.jdbclight.sql.SQLBuilder.batchOf;
import static com.hadzhy.jdbclight.sql.SQLBuilder.delete;

@Singleton
public class DBManagementUtils {

    private final OTPRepository otpRepository;

    private final HOTPGenerator hotpGenerator;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JDBC jdbc = JDBC.instance();

    public static final String DELETE_USER = batchOf(
            delete()
                    .from("otp")
                    .where("user_id = (SELECT id FROM user_account WHERE email = ?)")
                    .build()
                    .toSQlQuery(),
            delete()
                    .from("refresh_token")
                    .where("user_id = (SELECT id FROM user_account WHERE email = ?)")
                    .build()
                    .toSQlQuery(),
            delete()
                    .from("user_account")
                    .where("email = ?")
                    .build()
                    .toSQlQuery());

    DBManagementUtils(
            UserRepository userRepository,
            OTPRepository otpRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.hotpGenerator = new HOTPGenerator();
    }

    public OTP saveUser(RegistrationForm form) {
        String encodedPassword = passwordEncoder.encode(form.password());
        User user = User.of(
                new PersonalData(form.firstname(), form.surname(), form.phone(), encodedPassword, form.email(), form.birthDate()),
                HOTPGenerator.generateSecretKey()
        );
        userRepository.save(user);

        OTP otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));
        otpRepository.save(otp);
        return otp;
    }

    public OTP getUserOTP(String email) {
        User user = Objects.requireNonNull(userRepository.findBy(new Email(email)).orElseThrow());
        return otpRepository.findBy(user.id()).orElseThrow();
    }

    public void saveVerifiedUser(RegistrationForm form) {
        OTP otp = saveUser(form);
        User user = Objects.requireNonNull(userRepository.findBy(new Email(form.email())).orElseThrow());

        user.incrementCounter();
        otp.confirm();
        userRepository.updateCounter(user);
        otpRepository.updateConfirmation(otp);

        user.enable();
        userRepository.updateVerification(user);
    }

    public void removeUser(String email) {
        jdbc.write(DELETE_USER, email, email, email);
    }
}
