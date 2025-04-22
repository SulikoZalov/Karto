package org.project.karto.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.user.entities.User;
import org.project.karto.domain.user.repository.UserRepository;
import org.project.karto.domain.user.values_objects.Email;
import org.project.karto.domain.user.values_objects.Phone;
import org.project.karto.domain.user.values_objects.RefreshToken;

import java.util.UUID;

@ApplicationScoped
public class JDBCUserRepository implements UserRepository {

    // TODO

    @Override
    public void save(User user) {

    }

    @Override
    public void saveRefreshToken(RefreshToken refreshToken) {

    }

    @Override
    public void update(User user) {

    }

    @Override
    public void updateCounter(User user) {

    }

    @Override
    public void updateVerification(User user) {

    }

    @Override
    public void removeRefreshToken(RefreshToken refreshToken) {

    }

    @Override
    public boolean isEmailExists(Email email) {
        return false;
    }

    @Override
    public boolean isPhoneExists(Phone phone) {
        return false;
    }

    @Override
    public void updatePhone(User user) {

    }

    @Override
    public Result<User, Throwable> findBy(UUID id) {
        return null;
    }

    @Override
    public Result<User, Throwable> findBy(Email email) {
        return null;
    }

    @Override
    public Result<User, Throwable> findBy(Phone phone) {
        return null;
    }

    @Override
    public Result<RefreshToken, Throwable> findRefreshToken(String refreshToken) {
        return null;
    }
}
