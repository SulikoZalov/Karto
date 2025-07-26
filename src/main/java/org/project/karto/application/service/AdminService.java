package org.project.karto.application.service;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.project.karto.application.dto.auth.CompanyRegistrationForm;
import org.project.karto.application.dto.auth.Token;
import org.project.karto.domain.common.value_objects.CardUsageLimitations;
import org.project.karto.domain.common.value_objects.Email;
import org.project.karto.domain.common.value_objects.Password;
import org.project.karto.domain.common.value_objects.Phone;
import org.project.karto.domain.companies.entities.Company;
import org.project.karto.domain.companies.entities.PartnerVerificationOTP;
import org.project.karto.domain.companies.repository.CompanyRepository;
import org.project.karto.domain.companies.repository.PartnerVerificationOTPRepository;
import org.project.karto.domain.companies.value_objects.CompanyName;
import org.project.karto.domain.companies.value_objects.RegistrationNumber;
import org.project.karto.domain.user.entities.User;
import org.project.karto.domain.user.repository.UserRepository;
import org.project.karto.domain.companies.value_objects.PictureOfCards;
import org.project.karto.infrastructure.communication.PhoneInteractionService;
import org.project.karto.infrastructure.files.CardPicturesRepository;
import org.project.karto.infrastructure.files.StreamUtils;
import org.project.karto.infrastructure.security.HOTPGenerator;
import org.project.karto.infrastructure.security.JWTUtility;
import org.project.karto.infrastructure.security.PasswordEncoder;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.project.karto.application.util.RestUtil.responseException;

@ApplicationScoped
public class AdminService {

    @ConfigProperty(name = "admin.verification.key")
    String verificationKey;

    private final JWTUtility jwtUtility;

    private final HOTPGenerator hotpGenerator;

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    private final CompanyRepository companyRepository;

    private final CardPicturesRepository picturesRepository;

    private final PhoneInteractionService phoneInteractionService;

    private final PartnerVerificationOTPRepository otpRepository;

    AdminService(
            JWTUtility jwtUtility,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            CompanyRepository companyRepository,
            CardPicturesRepository picturesRepository,
            PhoneInteractionService phoneInteractionService,
            PartnerVerificationOTPRepository otpRepository) {

        this.jwtUtility = jwtUtility;
        this.userRepository = userRepository;
        this.picturesRepository = picturesRepository;
        this.phoneInteractionService = phoneInteractionService;
        this.hotpGenerator = new HOTPGenerator();
        this.passwordEncoder = passwordEncoder;
        this.companyRepository = companyRepository;
        this.otpRepository = otpRepository;
    }

    public Token auth(String verificationKey) {
        if (!secureEquals(this.verificationKey, verificationKey))
            throw responseException(Response.Status.FORBIDDEN, "Invalid administrator verification key.");

        return new Token(jwtUtility.generateAdministratorToken());
    }

    private boolean secureEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    public void registerPartner(CompanyRegistrationForm registrationForm) {
        if (registrationForm == null)
            throw responseException(Response.Status.BAD_REQUEST, "Company registration form must be filled.");

        Password.validate(registrationForm.rawPassword());

        CompanyName companyName = new CompanyName(registrationForm.companyName());
        if (companyRepository.isExists(companyName))
            throw responseException(Response.Status.CONFLICT, "Company name already exists.");

        RegistrationNumber registrationNumber = new RegistrationNumber(registrationForm.registrationCountryCode(),
                registrationForm.registrationNumber());
        if (companyRepository.isExists(registrationNumber))
            throw responseException(Response.Status.CONFLICT, "Registration number already exists.");

        Phone phone = new Phone(registrationForm.phone());
        if (companyRepository.isExists(phone))
            throw responseException(Response.Status.CONFLICT, "Phone already exists.");

        Email email = new Email(registrationForm.email());
        if (companyRepository.isExists(email))
            throw responseException(Response.Status.CONFLICT, "Email already exists.");

        Password encodedPassword = new Password(passwordEncoder.encode(registrationForm.rawPassword()));

        Company company = Company.of(
                registrationNumber,
                companyName,
                email,
                phone,
                encodedPassword,
                HOTPGenerator.generateSecretKey(),
                CardUsageLimitations.of(registrationForm.cardExpirationDays(), registrationForm.cardMaxUsageCount()));

        companyRepository.save(company)
                .orElseThrow(() -> responseException(Response.Status.INTERNAL_SERVER_ERROR,
                        "Unable to save company. Please try again later."));

        generateAndResendPartnerOTP(company);
    }

    public void putPartnerCardPicture(InputStream inputStream, String companyName) {
        Company company = companyRepository.findBy(new CompanyName(companyName))
                .orElseThrow(() -> responseException(Status.NOT_FOUND, "This partner is not found."));

        final byte[] pictureBytes = StreamUtils.toByteArray(inputStream)
                .orElseThrow(() -> responseException(Status.BAD_REQUEST, "Invalid picture."));

        final PictureOfCards picture = PictureOfCards.of(pictureBytes, company);
        company.addPicture(picture);

        picturesRepository.put(company);
    }

    public PictureOfCards loadProfilePicture(String companyName) {
        Company company = companyRepository.findBy(new CompanyName(companyName))
                .orElseThrow(() -> responseException(Status.NOT_FOUND, "This partner is not found."));

        return picturesRepository.load(PictureOfCards.profilePicturePath(company.id().toString()))
                .orElseThrow(() -> responseException(Status.NOT_FOUND, "Picture not found."));
    }

    public void banUser(String receivedPhone) {
        Phone phone = new Phone(receivedPhone);
        User user = userRepository.findBy(phone).orElseThrow();

        user.ban();
        userRepository.updateBan(user)
                .orElseThrow(() -> responseException(Response.Status.INTERNAL_SERVER_ERROR, "Please try again later."));
    }

    private void generateAndResendPartnerOTP(Company company) {
        PartnerVerificationOTP otp = PartnerVerificationOTP
                .of(company,
                        hotpGenerator.generateHOTP(company.keyAndCounter().key(), company.keyAndCounter().counter()));

        otpRepository.save(otp)
                .orElseThrow(() -> responseException(Response.Status.INTERNAL_SERVER_ERROR,
                        "Unable to process your request at the moment. Please try again."));

        company.incrementCounter();

        companyRepository.updateCounter(company)
                .orElseThrow(() -> {
                    otpRepository.remove(otp).ifFailure(throwable -> Log.error("Can`t remove otp.", throwable));
                    return responseException(Response.Status.INTERNAL_SERVER_ERROR,
                            "Unable to process your request at the moment. Please try again.");
                });

        phoneInteractionService.sendOTP(company.phone(), otp);
    }
}
