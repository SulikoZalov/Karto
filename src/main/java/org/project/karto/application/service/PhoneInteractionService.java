package org.project.karto.application.service;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.project.karto.domain.user.entities.OTP;
import org.project.karto.domain.user.values_objects.Phone;

@ApplicationScoped
public class PhoneInteractionService {

    // TODO implement when getting a credentials

    private static final String KARTO_PHONE = "";

    public void sendOTP(Phone phone, OTP otp) {
        Log.infof("Sending otp for user: %s".formatted(otp.userID()));
        //Message.creator(new PhoneNumber(phone.phoneNumber()), new PhoneNumber(KARTO_PHONE), otp.otp()).create();
    }

    public void sendMessage(Phone phone, String message) {
        //Message.creator(new PhoneNumber(phone.phoneNumber()), new PhoneNumber(KARTO_PHONE), message).create();
    }
}