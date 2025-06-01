package org.project.karto.domain.user.values_objects;

import org.project.karto.domain.common.annotations.Nullable;
import org.project.karto.domain.common.value_objects.Email;
import org.project.karto.domain.common.value_objects.Phone;

import java.time.LocalDate;
import java.util.Optional;

public final class PersonalData {
    private final String firstname;
    private final String surname;
    private final String email;
    private final LocalDate birthDate;
    private final String phone;
    private final String password;

    public PersonalData(
            String firstname,
            String surname,
            @Nullable String phone,
            @Nullable String password,
            String email,
            LocalDate birthDate) {

        Firstname.validate(firstname);
        Surname.validate(surname);
        if (phone != null) Phone.validate(phone);
        Email.validate(email);
        Birthdate.validate(birthDate);

        this.firstname = firstname;
        this.surname = surname;
        this.phone = phone;
        this.password = password;
        this.email = email;
        this.birthDate = birthDate;
    }

    public String firstname() {
        return firstname;
    }

    public String surname() {
        return surname;
    }

    public Optional<String> phone() {
        return Optional.ofNullable(phone);
    }

    public Optional<String> password() {
        return Optional.ofNullable(password);
    }

    public String email() {
        return email;
    }

    public LocalDate birthDate() {
        return birthDate;
    }
}