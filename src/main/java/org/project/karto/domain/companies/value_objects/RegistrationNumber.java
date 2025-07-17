package org.project.karto.domain.companies.value_objects;

import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record RegistrationNumber(String countryCode, String value) {
    private static final Set<String> COUNTRY_CODES = Set.of(Locale.getISOCountries());
    private static final int COUNTRY_CODE_LENGTH = 2;
    private static final int REG_NUMBER_MIN_LENGTH = 5;
    private static final int REG_NUMBER_MAX_LENGTH = 20;
    private static final String REGISTRATION_NUMBER_REGEX = "[A-Z0-9\\-\\s]{5,20}";
    private static final Pattern REGISTRATION_NUMBER_PATTERN = Pattern.compile(REGISTRATION_NUMBER_REGEX);

    public RegistrationNumber {
        if (countryCode == null || countryCode.isBlank())
            throw new IllegalDomainArgumentException("Country code must be provided.");

        if (countryCode.length() != COUNTRY_CODE_LENGTH)
            throw new IllegalDomainArgumentException("Country code must be 2 letters.");

        if (!COUNTRY_CODES.contains(countryCode))
            throw new IllegalDomainArgumentException("This country code do not exists.");

        if (value == null || value.isBlank())
            throw new IllegalDomainArgumentException("Registration number must be provided.");

        String trimmed = value.trim();
        if (trimmed.length() < REG_NUMBER_MIN_LENGTH || trimmed.length() > REG_NUMBER_MAX_LENGTH)
            throw new IllegalDomainArgumentException("Registration number must be 5 to 20 characters long.");

        Matcher matcher = REGISTRATION_NUMBER_PATTERN.matcher(trimmed);
        if (!matcher.matches())
            throw new IllegalDomainArgumentException("Invalid registration number format.");
    }
}
