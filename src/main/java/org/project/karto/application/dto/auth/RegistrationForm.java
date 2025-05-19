package org.project.karto.application.dto.auth;

import java.time.LocalDate;

public record RegistrationForm(
        String firstname,
        String surname,
        String phone,
        String email,
        String password,
        String passwordConfirmation,
        LocalDate birthDate
) {}