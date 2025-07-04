package org.project.karto.domain.common.value_objects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Phone(String phoneNumber) {

    public static final String PHONE_NUMBER_REGEX = "^(\\+\\d{1,3}( )?)?((\\(\\d{1,3}\\))|\\d{1,3})[- .]?\\d{3,4}[- .]?\\d{4}$";
    public static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile(PHONE_NUMBER_REGEX);
    public static final int MAX_SIZE = 22;

    public Phone {
        validate(phoneNumber);
    }

    public static void validate(String phoneNumber) {
        if (phoneNumber == null) throw new IllegalArgumentException("Phone number can`t be null");
        if (phoneNumber.isBlank()) throw new IllegalArgumentException("Phone number should`t be blank.");
        if (phoneNumber.length() > MAX_SIZE) throw new IllegalArgumentException("Phone number is too long");

        Matcher matcher = PHONE_NUMBER_PATTERN.matcher(phoneNumber);
        if (!matcher.matches()) throw new IllegalArgumentException("Invalid phone number.");
    }

    @Override
    public String toString() {
        return phoneNumber;
    }
}