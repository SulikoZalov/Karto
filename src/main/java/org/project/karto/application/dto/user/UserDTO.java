package org.project.karto.application.dto.user;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserDTO(
        String firstname,
        String surname,
        String email,
        String phone,
        LocalDate birthDate,
        boolean isVerified,
        boolean is2FAEnabled,
        BigDecimal storedCashback) {
}
