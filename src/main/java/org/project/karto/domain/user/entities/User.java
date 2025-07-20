package org.project.karto.domain.user.entities;

import org.project.karto.domain.common.enumerations.Role;
import org.project.karto.domain.common.exceptions.IllegalDomainArgumentException;
import org.project.karto.domain.common.exceptions.IllegalDomainStateException;
import org.project.karto.domain.common.value_objects.Amount;
import org.project.karto.domain.common.value_objects.KeyAndCounter;
import org.project.karto.domain.common.value_objects.Phone;
import org.project.karto.domain.user.exceptions.BannedUserException;
import org.project.karto.domain.user.values_objects.CashbackStorage;
import org.project.karto.domain.user.values_objects.PersonalData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class User {
    private final UUID id;
    private PersonalData personalData;
    private final Role role;
    private boolean isVerified;
    private boolean is2FAEnabled;
    private boolean isBanned;
    private KeyAndCounter keyAndCounter;
    private CashbackStorage cashbackStorage;
    private final LocalDateTime creationDate;
    private final LocalDateTime lastUpdated;

    private User(
            UUID id,
            PersonalData personalData,
            boolean isVerified,
            boolean is2FAEnabled,
            boolean isBanned,
            KeyAndCounter keyAndCounter,
            CashbackStorage cashbackStorage,
            LocalDateTime creationDate,
            LocalDateTime lastUpdated) throws BannedUserException {

        if (id == null) throw new IllegalDomainArgumentException("id must not be null");
        if (personalData == null) throw new IllegalDomainArgumentException("personalData must not be null");
        if (keyAndCounter == null) throw new IllegalDomainArgumentException("keyAndCounter must not be null");
        if (cashbackStorage == null) throw new IllegalDomainArgumentException("cashbackStorage must not be null");
        if (creationDate == null) throw new IllegalDomainArgumentException("creationDate must not be null");
        if (lastUpdated == null) throw new IllegalDomainArgumentException("lastUpdated must not be null");
        if (isBanned) throw new BannedUserException("Access denied: this user account has been banned due to a violation of platform rules. Contact support for further assistance.");

        this.id = id;
        this.personalData = personalData;
        this.role = Role.CUSTOMER;
        this.isVerified = isVerified;
        this.is2FAEnabled = is2FAEnabled;
        this.keyAndCounter = keyAndCounter;
        this.cashbackStorage = cashbackStorage;
        this.creationDate = creationDate;
        this.lastUpdated = lastUpdated;
    }

    public static User of(PersonalData personalData, String key) {
        return new User(UUID.randomUUID(),
                personalData,
                false,
                false,
                false,
                new KeyAndCounter(key, 0),
                new CashbackStorage(BigDecimal.ZERO),
                LocalDateTime.now(),
                LocalDateTime.now());
    }

    public static User fromRepository(
            UUID id,
            PersonalData personalData,
            boolean isEnabled,
            boolean is2FAVerified,
            boolean isBanned,
            KeyAndCounter keyAndCounter,
            CashbackStorage cashbackStorage,
            LocalDateTime creationDate,
            LocalDateTime lastUpdated) throws BannedUserException {

        return new User(id,
                personalData,
                isEnabled,
                is2FAVerified,
                isBanned,
                keyAndCounter,
                cashbackStorage,
                creationDate,
                lastUpdated);
    }

    public UUID id() {
        return id;
    }

    public PersonalData personalData() {
        return personalData;
    }

    public Role role() {
        return role;
    }

    public void registerPhoneForVerification(Phone phone) {
        if (phone == null)
            throw new IllegalDomainArgumentException("Phone is null");

        if (this.personalData.phone().isPresent())
            throw new IllegalDomainArgumentException("Phone number already registered.");

        this.personalData = new PersonalData(
                personalData().firstname(),
                personalData().surname(),
                phone.phoneNumber(),
                personalData().password().orElse(null),
                personalData().email(),
                personalData().birthDate()
        );
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void enable() {
        if (isVerified)
            throw new IllegalDomainStateException("You can`t active already verified user.");
        if (keyAndCounter.counter() == 0)
            throw new IllegalDomainStateException("It is prohibited to activate an account that has not been verified.");
        this.isVerified = true;
    }

    public void enable2FA() {
        if (!isVerified)
            throw new IllegalDomainStateException("You can`t enable 2FA on not verified account");
        if (keyAndCounter.counter() == 0 || keyAndCounter.counter() == 1)
            throw new IllegalDomainStateException("Counter need to be incremented");
        if (is2FAEnabled)
            throw new IllegalDomainStateException("You can`t activate 2FA twice");

        this.is2FAEnabled = true;
    }

    public boolean canLogin() {
        return isVerified && !isBanned;
    }

    public boolean isBanned() {
        return isBanned;
    }

    public void ban() {
        if (isBanned) throw new IllegalDomainStateException("User is already banned.");
        this.isBanned = true;
    }

    public boolean is2FAEnabled() {
        return is2FAEnabled;
    }

    public void disable() {
        if (!isVerified)
            throw new IllegalDomainStateException("You can't deactivate a user who is already deactivated.");
        this.isVerified = false;
    }

    public KeyAndCounter keyAndCounter() {
        return keyAndCounter;
    }

    public void incrementCounter() {
        this.keyAndCounter = new KeyAndCounter(keyAndCounter.key(), keyAndCounter.counter() + 1);
    }

    public CashbackStorage cashbackStorage() {
        return cashbackStorage;
    }

    public void addCashback(Amount amount) {
        if (amount == null)
            throw new IllegalDomainArgumentException("Amount can`t be null");
        if (!isVerified)
            throw new IllegalDomainArgumentException("Account is not verified");

        cashbackStorage = new CashbackStorage(cashbackStorage.amount().add(amount.value()));
    }

    public void removeCashFromStorage(Amount amount) {
        if (amount == null)
            throw new IllegalDomainArgumentException("Amount can`t be null");
        if (!isVerified)
            throw new IllegalDomainArgumentException("Account is not verified");

        BigDecimal remainingSum = cashbackStorage.amount().subtract(amount.value());
        if (remainingSum.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalDomainArgumentException("Insufficient funds in cashback storage");

        cashbackStorage = new CashbackStorage(remainingSum);
    }

    public LocalDateTime creationDate() {
        return creationDate;
    }

    public LocalDateTime lastUpdated() {
        return lastUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return """
                User: {
                    ID: %s,
                    Firstname: %s,
                    Lastname: %s,
                    Phone: %s,
                    Email: %s,
                    Birth date: %s
                }
                """.formatted(id.toString(),
                personalData.firstname(),
                personalData.surname(),
                personalData.phone(),
                personalData.email(),
                personalData.birthDate());
    }
}
