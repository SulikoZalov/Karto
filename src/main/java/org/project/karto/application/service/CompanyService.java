package org.project.karto.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.project.karto.application.dto.auth.LoginForm;
import org.project.karto.application.dto.auth.Token;
import org.project.karto.domain.common.value_objects.CardUsageLimitations;
import org.project.karto.domain.common.value_objects.Phone;
import org.project.karto.domain.companies.entities.Company;
import org.project.karto.domain.companies.entities.PartnerVerificationOTP;
import org.project.karto.domain.companies.repository.CompanyRepository;
import org.project.karto.domain.companies.repository.PartnerVerificationOTPRepository;
import org.project.karto.domain.companies.value_objects.CompanyName;
import org.project.karto.domain.user.values_objects.Password;
import org.project.karto.infrastructure.communication.PhoneInteractionService;
import org.project.karto.infrastructure.security.HOTPGenerator;
import org.project.karto.infrastructure.security.JWTUtility;
import org.project.karto.infrastructure.security.PasswordEncoder;

import static org.project.karto.application.util.RestUtil.responseException;

@ApplicationScoped
public class CompanyService {

    private final JWTUtility jwtUtility;

    private final HOTPGenerator hotpGenerator;

    private final PasswordEncoder passwordEncoder;

    private final CompanyRepository companyRepository;

    private final PhoneInteractionService phoneInteractionService;

    private final PartnerVerificationOTPRepository otpRepository;

    CompanyService(JWTUtility jwtUtility, PasswordEncoder passwordEncoder,
                   CompanyRepository companyRepository,
                   PhoneInteractionService phoneInteractionService,
                   PartnerVerificationOTPRepository otpRepository) {

        this.jwtUtility = jwtUtility;
        this.passwordEncoder = passwordEncoder;
        this.phoneInteractionService = phoneInteractionService;
        this.hotpGenerator = new HOTPGenerator();
        this.companyRepository = companyRepository;
        this.otpRepository = otpRepository;
    }

    public void resendPartnerOTP(String phoneNumber) {
        Phone phone = new Phone(phoneNumber);
        Company company = companyRepository.findBy(phone)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "Company not found."));
        PartnerVerificationOTP otp = otpRepository.findBy(company.id())
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "OTP not exists. Old one must be for resend."));

        otpRepository.remove(otp);
        generateAndResendPartnerOTP(company);
    }

    public void verifyPartnerAccount(String receivedOTP) {
        try {
            PartnerVerificationOTP.validate(receivedOTP);
            PartnerVerificationOTP otp = otpRepository.findBy(receivedOTP)
                    .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "OTP not found."));
            Company company = companyRepository.findBy(otp.companyID())
                    .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "User not found."));

            if (company.isActive())
                throw responseException(Response.Status.BAD_REQUEST, "User already verified.");

            if (otp.isExpired())
                throw responseException(Response.Status.GONE, "OTP is gone.");

            otp.confirm();
            otpRepository.updateConfirmation(otp);

            company.enable();
            companyRepository.updateVerification(company);
        } catch (IllegalStateException e) {
            throw responseException(Response.Status.FORBIDDEN, e.getMessage());
        }
    }

    public Token login(LoginForm loginForm) {
        Password.validate(loginForm.password());
        Phone phone = new Phone(loginForm.phone());

        Company company = companyRepository.findBy(phone)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "Company not found."));

        if (!company.isActive())
            throw responseException(Response.Status.FORBIDDEN, "You can`t login with unverified account.");

        final boolean isValidPasswordProvided = passwordEncoder.verify(loginForm.password(), company.password().password());
        if (!isValidPasswordProvided)
            throw responseException(Response.Status.BAD_REQUEST, "Password do not match.");

        return new Token(jwtUtility.generateToken(company));
    }

    public void changePassword(String rawPassword, String receivedCompanyName) {
        Password.validate(rawPassword);
        CompanyName companyName = new CompanyName(receivedCompanyName);

        Company company = companyRepository.findBy(companyName)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "This company not found."));
        Password encodedPassword = new Password(passwordEncoder.encode(rawPassword));

        company.changePassword(encodedPassword);
        companyRepository.updatePassword(company);
    }

    public void changeCardLimitations(int days, int maxUsageCount, String receivedCompanyName) {
        CardUsageLimitations limitations = CardUsageLimitations.of(days, maxUsageCount);
        CompanyName companyName = new CompanyName(receivedCompanyName);

        Company company = companyRepository.findBy(companyName)
                .orElseThrow(() -> responseException(Response.Status.NOT_FOUND, "This company not found."));

        company.specifyCardUsageLimitations(limitations);
        companyRepository.updateCardUsageLimitations(company);
    }

    private void generateAndResendPartnerOTP(Company company) {
        PartnerVerificationOTP otp = PartnerVerificationOTP
                .of(company, hotpGenerator.generateHOTP(company.keyAndCounter().key(), company.keyAndCounter().counter()));

        otpRepository.save(otp);
        company.incrementCounter();
        companyRepository.updateCounter(company);
        phoneInteractionService.sendOTP(company.phone(), otp);
    }
}
