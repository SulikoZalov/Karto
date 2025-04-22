package org.project.karto.application.service;

import io.quarkus.logging.Log;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.project.karto.application.dto.LateVerificationForm;
import org.project.karto.application.dto.LoginForm;
import org.project.karto.application.dto.RegistrationForm;
import org.project.karto.application.dto.Tokens;
import org.project.karto.domain.user.entities.OTP;
import org.project.karto.domain.user.entities.User;
import org.project.karto.domain.user.repository.OTPRepository;
import org.project.karto.domain.user.repository.UserRepository;
import org.project.karto.domain.user.values_objects.*;
import org.project.karto.infrastructure.security.HOTPGenerator;
import org.project.karto.infrastructure.security.JwtUtility;
import org.project.karto.infrastructure.security.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import static org.project.karto.application.util.RestUtil.responseException;

@ApplicationScoped
public class AuthService {

    private final JwtUtility jwtUtility;

    private final HOTPGenerator hotpGenerator;

    private final OTPRepository otpRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final SecurityIdentity securityIdentity;

    private final EmailInteractionService emailInteractionService;

    private final PhoneInteractionService phoneInteractionService;

    AuthService(
            JwtUtility jwtUtility,
            UserRepository userRepository,
            OTPRepository otpRepository,
            EmailInteractionService emailInteractionService,
            PhoneInteractionService phoneInteractionService,
            PasswordEncoder passwordEncoder,
            SecurityIdentity securityIdentity) {

        this.jwtUtility = jwtUtility;
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.emailInteractionService = emailInteractionService;
        this.phoneInteractionService = phoneInteractionService;
        this.passwordEncoder = passwordEncoder;
        this.securityIdentity = securityIdentity;
        this.hotpGenerator = new HOTPGenerator();
    }

    public void registration(RegistrationForm registrationForm) {
        try {
            if (registrationForm == null)
                throw responseException(Response.Status.BAD_REQUEST, "Registration for is null");

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

            OTP otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));
            otpRepository.save(otp);

            phoneInteractionService.sendOTP(phone, otp);
            emailInteractionService.sendSoftVerificationMessage(email);
        } catch (IllegalArgumentException e) {
            throw responseException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    public void resendOTP(String phoneNumber) {
        try {
            Phone phone = new Phone(phoneNumber);
            User user = userRepository.findBy(phone)
                    .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "User not found."));
            OTP otp = otpRepository.findBy(user.id())
                    .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "OTP not exists. Old one must be for resend."));

            user.incrementCounter();
            userRepository.updateCounter(user);
            otpRepository.remove(otp);

            OTP regeneratedOTP = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));
            otpRepository.save(regeneratedOTP);

            phoneInteractionService.sendOTP(phone, regeneratedOTP);
        } catch (IllegalArgumentException e) {
            throw responseException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    public void lateVerification(LateVerificationForm lvForm) {
        try {
            Email email = new Email(lvForm.email());
            Phone phone = new Phone(lvForm.phone());

            if (userRepository.isPhoneExists(phone))
                throw responseException(Response.Status.CONFLICT, "Phone already used.");

            User user = userRepository.findBy(email)
                    .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "User not found."));

            user.incrementCounter();
            userRepository.updateCounter(user);

            user.registerPhoneForVerification(phone);
            userRepository.updatePhone(user);

            OTP otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));
            otpRepository.save(otp);

            phoneInteractionService.sendOTP(phone, otp);
            emailInteractionService.sendSoftVerificationMessage(email);
        } catch (IllegalArgumentException e) {
            throw responseException(Response.Status.BAD_REQUEST, e.getMessage());
        }
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

            user.incrementCounter();
            otp.confirm();
            userRepository.updateCounter(user);
            otpRepository.update(otp);

            user.enable();
            userRepository.updateVerification(user);
        } catch (IllegalArgumentException e) {
            throw responseException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    public Tokens login(LoginForm loginForm) {
        try {
            Password.validate(loginForm.password());
            Phone phone = new Phone(loginForm.phone());

            User user = userRepository.findBy(phone)
                    .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "User not found."));

            final boolean isValidPasswordProvided = passwordEncoder.verify(loginForm.password(),
                    user.personalData().password().orElseThrow());
            if (!isValidPasswordProvided)
                throw responseException(Response.Status.BAD_REQUEST, "Password do not match.");

            Tokens tokens = generateTokens(user);
            userRepository.saveRefreshToken(new RefreshToken(user.id(), tokens.refreshToken()));
            return tokens;
        } catch (IllegalArgumentException e) {
            throw responseException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    public Tokens login(String idToken) {
        try {
            String emailAttribute = securityIdentity.getAttribute("email");
            Email email = new Email(emailAttribute);

            if (!userRepository.isEmailExists(email)) {
                User user = registerNonExistedUser(email);
                Tokens tokens = generateTokens(user);
                userRepository.saveRefreshToken(new RefreshToken(user.id(), tokens.refreshToken()));
                return tokens;
            }

            User user = userRepository.findBy(email)
                    .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "Unexpected. Registered user not found."));
            Tokens tokens = generateTokens(user);
            userRepository.saveRefreshToken(new RefreshToken(user.id(), tokens.refreshToken()));
            return tokens;
        } catch (IllegalArgumentException e) {
            throw responseException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    public String refreshToken(String refreshToken) {
        if (refreshToken == null)
            throw responseException(Response.Status.BAD_REQUEST, "Refresh token can`t be null");

        RefreshToken foundedPairResult = userRepository.findRefreshToken(refreshToken)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "This refresh token is not found."));

        long tokenExpirationDate = jwtUtility.parseJWT(foundedPairResult.refreshToken())
                .orElseThrow(() -> responseException(Response.Status.BAD_REQUEST, "Something went wrong, try again later."))
                .getExpirationTime();

        var tokenExpiration = LocalDateTime.ofEpochSecond(tokenExpirationDate, 0, ZoneOffset.UTC);

        if (LocalDateTime.now(ZoneOffset.UTC).isAfter(tokenExpiration)) {
            userRepository.removeRefreshToken(foundedPairResult);
            throw responseException(Response.Status.BAD_REQUEST, "Refresh token is expired, you need to login.");
        }

        final User user = userRepository
                .findBy(foundedPairResult.userID())
                .orElseThrow(() -> {
                    Log.error("User is not found");
                    return responseException(Response.Status.NOT_FOUND, "User not found.");
                });

        return jwtUtility.generateToken(user);
    }

    private User registerNonExistedUser(Email email) {
        Password.validate(securityIdentity.getAttribute("password"));
        String encodedPassword = passwordEncoder.encode(securityIdentity.getAttribute("password"));

        String firstname = securityIdentity.getAttribute("firstname");
        String surname = securityIdentity.getAttribute("lastname");
        String phone = securityIdentity.getAttribute("phone");
        LocalDate birthDate = securityIdentity.getAttribute("birthDate");

        PersonalData personalData = new PersonalData(
                firstname,
                surname,
                phone,
                encodedPassword,
                email.email(),
                birthDate
        );
        String secretKey = HOTPGenerator.generateSecretKey();

        User user = User.of(personalData, secretKey);
        userRepository.save(user);
        emailInteractionService.sendSoftVerificationMessage(email);
        return user;
    }

    private Tokens generateTokens(User user) {
        String token = jwtUtility.generateToken(user);
        String refreshToken = jwtUtility.generateRefreshToken(user);
        return new Tokens(token, refreshToken);
    }
}
