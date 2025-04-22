package org.project.karto.domain.user.repository;

import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.user.entities.User;
import org.project.karto.domain.user.values_objects.Email;
import org.project.karto.domain.user.values_objects.Phone;
import org.project.karto.domain.user.values_objects.RefreshToken;

import java.util.UUID;

public interface UserRepository {

    void save(User user);

    void saveRefreshToken(RefreshToken refreshToken);

    void update(User user);

    void updatePhone(User user);

    void updateCounter(User user);

    void updateVerification(User user);

    void removeRefreshToken(RefreshToken refreshToken);

    boolean isEmailExists(Email email);

    boolean isPhoneExists(Phone phone);

    Result<User, Throwable> findBy(UUID id);

    Result<User, Throwable> findBy(Email email);

    Result<User, Throwable> findBy(Phone phone);

    Result<RefreshToken, Throwable> findRefreshToken(String refreshToken);
}
