package org.project.karto.domain.companies.value_objects;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record RegistrationNumber(String countryCode, String value) {
    private static final Set<String> COUNTRY_CODES = Set.of(Locale.getISOCountries());
    private static final String REGISTRATION_NUMBER_REGEX = "[A-Z0-9\\-\\s]{5,20}";
    private static final Pattern REGISTRATION_NUMBER_PATTERN = Pattern.compile(REGISTRATION_NUMBER_REGEX);

    public RegistrationNumber {
        if (countryCode == null || countryCode.isBlank())
            throw new IllegalArgumentException("Country code must be provided.");

        if (value == null || value.isBlank())
            throw new IllegalArgumentException("Registration number must be provided.");

        if (!COUNTRY_CODES.contains(countryCode))
            throw new IllegalArgumentException("This country code do not exists.");

        Matcher matcher = REGISTRATION_NUMBER_PATTERN.matcher(value.trim());
        if (!matcher.matches())
            throw new IllegalArgumentException("Invalid registration number format.");
    }
}
