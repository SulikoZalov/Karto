package org.project.karto.application.service;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.project.karto.application.dto.auth.*;
import org.project.karto.domain.common.value_objects.Email;
import org.project.karto.domain.common.value_objects.Phone;
import org.project.karto.domain.user.entities.OTP;
import org.project.karto.domain.user.entities.User;
import org.project.karto.domain.user.repository.OTPRepository;
import org.project.karto.domain.user.repository.UserRepository;
import org.project.karto.domain.user.values_objects.*;
import org.project.karto.infrastructure.communication.EmailInteractionService;
import org.project.karto.infrastructure.communication.PhoneInteractionService;
import org.project.karto.infrastructure.security.HOTPGenerator;
import org.project.karto.infrastructure.security.JWTUtility;
import org.project.karto.infrastructure.security.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import static org.project.karto.application.util.RestUtil.responseException;

@ApplicationScoped
public class AuthService {

    private final JWTUtility jwtUtility;

    private final HOTPGenerator hotpGenerator;

    private final OTPRepository otpRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final EmailInteractionService emailInteractionService;

    private final PhoneInteractionService phoneInteractionService;

    AuthService(
            JWTUtility jwtUtility,
            UserRepository userRepository,
            OTPRepository otpRepository,
            EmailInteractionService emailInteractionService,
            PhoneInteractionService phoneInteractionService,
            PasswordEncoder passwordEncoder) {

        this.jwtUtility = jwtUtility;
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.emailInteractionService = emailInteractionService;
        this.phoneInteractionService = phoneInteractionService;
        this.passwordEncoder = passwordEncoder;
        this.hotpGenerator = new HOTPGenerator();
    }

    public void registration(RegistrationForm registrationForm) {
        if (registrationForm == null)
            throw responseException(Response.Status.BAD_REQUEST, "Registration form is null");

        if (!Objects.equals(registrationForm.password(), registrationForm.passwordConfirmation())) {
            Log.errorf("Registration failure, passwords do not match");
            throw responseException(Response.Status.BAD_REQUEST, "Passwords do not match");
        }

        Password.validate(registrationForm.password());

        Email email = new Email(registrationForm.email());
        if (userRepository.isEmailExists(email))
            throw responseException(Response.Status.CONFLICT, "Email already used");

        Phone phone = new Phone(registrationForm.phone());
        if (userRepository.isPhoneExists(phone))
            throw responseException(Response.Status.CONFLICT, "Phone already used");

        String encodedPassword = passwordEncoder.encode(registrationForm.password());

        PersonalData personalData = new PersonalData(
                registrationForm.firstname(),
                registrationForm.surname(),
                registrationForm.phone(),
                encodedPassword,
                registrationForm.email(),
                registrationForm.birthDate()
        );
        String secretKey = HOTPGenerator.generateSecretKey();

        User user = User.of(personalData, secretKey);
        userRepository.save(user);

        generateAndSendOTP(user);
        emailInteractionService.sendSoftVerificationMessage(email);
    }

    public void resendOTP(String phoneNumber) {
        Phone phone = new Phone(phoneNumber);
        User user = userRepository.findBy(phone)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "User not found."));
        OTP otp = otpRepository.findBy(user.id())
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "OTP not exists. Old one must be for resend."));

        otpRepository.remove(otp);
        generateAndSendOTP(user);
    }

    public void lateVerification(LateVerificationForm lvForm) {
        Email email = new Email(lvForm.email());
        Phone phone = new Phone(lvForm.phone());

        if (userRepository.isPhoneExists(phone))
            throw responseException(Response.Status.CONFLICT, "Phone already used.");

        User user = userRepository.findBy(email)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "User not found."));

        user.registerPhoneForVerification(phone);
        userRepository.updatePhone(user);

        generateAndSendOTP(user);
        emailInteractionService.sendSoftVerificationMessage(email);
    }

    public void verification(String receivedOTP) {
        try {
            OTP.validate(receivedOTP);
            OTP otp = otpRepository.findBy(receivedOTP)
                    .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "OTP not found."));
            User user = userRepository.findBy(otp.userID())
                    .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "User not found."));

            if (user.isVerified())
                throw responseException(Response.Status.BAD_REQUEST, "User already verified.");

            if (otp.isExpired())
                throw responseException(Response.Status.GONE, "OTP is gone.");

            otp.confirm();
            otpRepository.updateConfirmation(otp);

            user.enable();
            userRepository.updateVerification(user);
        } catch (IllegalStateException e) {
            throw responseException(Response.Status.FORBIDDEN, e.getMessage());
        }
    }

    public Object login(LoginForm loginForm) {
        Password.validate(loginForm.password());
        Phone phone = new Phone(loginForm.phone());

        User user = userRepository.findBy(phone)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "User not found."));

        if (!user.isVerified())
            throw responseException(Response.Status.FORBIDDEN, "You can`t login with unverified account.");

        final boolean isValidPasswordProvided = passwordEncoder.verify(loginForm.password(),
                user.personalData().password().orElseThrow());
        if (!isValidPasswordProvided)
            throw responseException(Response.Status.BAD_REQUEST, "Password do not match.");

        if (user.is2FAEnabled()) {
            generateAndSendOTP(user);
            return TwoFAMessage.defaultMessage();
        }

        Tokens tokens = generateTokens(user);
        userRepository.saveRefreshToken(new RefreshToken(user.id(), tokens.refreshToken()));
        return tokens;
    }

    public void enable2FA(LoginForm loginForm) {
        if (loginForm == null)
            throw responseException(Response.Status.BAD_REQUEST, "Please fill the login form.");

        Password.validate(loginForm.password());
        Phone phone = new Phone(loginForm.phone());

        User user = userRepository.findBy(phone)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "User not found."));

        if (!user.isVerified())
            throw responseException(Response.Status.FORBIDDEN, "You can`t login with unverified account.");

        final boolean isValidPasswordProvided = passwordEncoder.verify(loginForm.password(),
                user.personalData().password().orElseThrow());
        if (!isValidPasswordProvided)
            throw responseException(Response.Status.BAD_REQUEST, "Password do not match.");

        if (otpRepository.contains(user.id()))
            throw responseException(Response.Status.BAD_REQUEST, "You can`t request 2FA activation twice");

        generateAndSendOTP(user);
    }

    public Tokens twoFactorAuth(String receivedOTP) {
        try {
            OTP.validate(receivedOTP);
            OTP otp = otpRepository.findBy(receivedOTP)
                    .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "OTP not found."));
            User user = userRepository.findBy(otp.userID())
                    .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "User not found."));

            if (!user.isVerified())
                throw responseException(Response.Status.FORBIDDEN, "You can`t login with unverified account.");

            if (!user.is2FAEnabled()) {
                Log.info("Two factor authentication is enabled and verified for user.");
                user.enable2FA();
                userRepository.update2FA(user);
            }

            Tokens tokens = generateTokens(user);
            userRepository.saveRefreshToken(new RefreshToken(user.id(), tokens.refreshToken()));
            return tokens;
        } catch (IllegalStateException e) {
            throw responseException(Response.Status.FORBIDDEN, e.getMessage());
        }
    }

    public Tokens oidcAuth(String idToken) {
        try {
            JsonWebToken claims = jwtUtility.verifyAndParse(idToken)
                    .orElseThrow(() -> responseException(Response.Status.FORBIDDEN, "Invalid id token"));

            String emailAttribute = claims.getClaim("email");
            Email email = new Email(emailAttribute);

            if (!userRepository.isEmailExists(email)) {
                User user = registerNonExistedUser(claims, email);
                Tokens tokens = generateTokens(user);
                userRepository.saveRefreshToken(new RefreshToken(user.id(), tokens.refreshToken()));
                return tokens;
            }

            User user = userRepository.findBy(email)
                    .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "Unexpected. Registered user not found."));
            Tokens tokens = generateTokens(user);
            userRepository.saveRefreshToken(new RefreshToken(user.id(), tokens.refreshToken()));
            return tokens;
        } catch (DateTimeParseException e) {
            throw responseException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    public Token refreshToken(String refreshToken) {
        if (refreshToken == null)
            throw responseException(Response.Status.BAD_REQUEST, "Refresh token can`t be null");

        RefreshToken foundedPairResult = userRepository.findRefreshToken(refreshToken)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "This refresh token is not found."));

        long tokenExpirationDate = jwtUtility.parse(foundedPairResult.refreshToken())
                .orElseThrow(() -> responseException(Response.Status.BAD_REQUEST, "Something went wrong, try again later."))
                .getExpirationTime();

        var tokenExpiration = LocalDateTime.ofEpochSecond(tokenExpirationDate, 0, ZoneOffset.UTC);

        if (LocalDateTime.now(ZoneOffset.UTC).isAfter(tokenExpiration))
            throw responseException(Response.Status.BAD_REQUEST, "Refresh token is expired, you need to login.");

        final User user = userRepository
                .findBy(foundedPairResult.userID())
                .orElseThrow(() -> {
                    Log.error("User is not found");
                    return responseException(Response.Status.NOT_FOUND, "User not found.");
                });

        String token = jwtUtility.generateToken(user);
        return new Token(token);
    }

    private User registerNonExistedUser(JsonWebToken claims, Email email) {
        String firstname = claims.getClaim("firstname");
        String surname = claims.getClaim("lastname");
        LocalDate birthDate = LocalDate.parse(claims.getClaim("birthDate"));

        PersonalData personalData = getPersonalData(email, firstname, surname, birthDate);
        String secretKey = HOTPGenerator.generateSecretKey();

        User user = User.of(personalData, secretKey);
        userRepository.save(user);
        emailInteractionService.sendSoftVerificationMessage(email);
        return user;
    }

    private static PersonalData getPersonalData(Email email, String firstname, String surname, LocalDate birthDate) {
        return new PersonalData(
                firstname,
                surname,
                null,
                null,
                email.email(),
                birthDate
        );
    }

    private Tokens generateTokens(User user) {
        String token = jwtUtility.generateToken(user);
        String refreshToken = jwtUtility.generateRefreshToken(user);
        return new Tokens(token, refreshToken);
    }

    private void generateAndSendOTP(User user) {
        OTP otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));
        otpRepository.save(otp);
        user.incrementCounter();
        userRepository.updateCounter(user);
        phoneInteractionService.sendOTP(new Phone(user.personalData().phone().orElseThrow()), otp);
    }
}
