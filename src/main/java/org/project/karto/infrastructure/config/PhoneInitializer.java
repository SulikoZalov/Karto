package org.project.karto.infrastructure.config;

import com.twilio.Twilio;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Startup
@ApplicationScoped
public class PhoneInitializer {

    @ConfigProperty(name = "phone.dev.account.sid")
    String accountSid;

    @ConfigProperty(name = "phone.dev.auth.token")
    String authToken;

    @PostConstruct
    void init() {
        Twilio.init(accountSid, authToken);
    }
}
