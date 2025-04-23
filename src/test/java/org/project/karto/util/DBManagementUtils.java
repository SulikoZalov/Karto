package org.project.karto.util;

import jakarta.inject.Singleton;
import org.project.karto.application.dto.RegistrationForm;
import org.project.karto.domain.user.entities.OTP;
import org.project.karto.domain.user.entities.User;
import org.project.karto.domain.user.repository.OTPRepository;
import org.project.karto.domain.user.repository.UserRepository;
import org.project.karto.domain.user.values_objects.Email;
import org.project.karto.domain.user.values_objects.PersonalData;
import org.project.karto.infrastructure.security.HOTPGenerator;

@Singleton
public class DBManagementUtils {

    private final HOTPGenerator hotpGenerator;

    private final UserRepository userRepository;

    private final OTPRepository otpRepository;

    DBManagementUtils(UserRepository userRepository, OTPRepository otpRepository) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.hotpGenerator = new HOTPGenerator();
    }

    public OTP saveUser(RegistrationForm form) {
        User user = User.of(
                new PersonalData(form.firstname(), form.surname(), form.phone(), form.password(), form.email(), form.birthDate()),
                HOTPGenerator.generateSecretKey()
        );
        userRepository.save(user);

        OTP otp = OTP.of(user, hotpGenerator.generateHOTP(user.keyAndCounter().key(), user.keyAndCounter().counter()));
        otpRepository.save(otp);
        return otp;
    }

    public OTP getUserOTP(String email) {
        User user = userRepository.findBy(new Email(email)).orElseThrow();
        return otpRepository.findBy(user.id()).orElseThrow();
    }
}
