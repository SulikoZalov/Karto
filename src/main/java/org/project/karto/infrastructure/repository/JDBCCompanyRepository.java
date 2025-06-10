package org.project.karto.infrastructure.repository;

import com.hadzhy.jetquerious.jdbc.JetQuerious;
import com.hadzhy.jetquerious.util.Result;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.project.karto.domain.common.value_objects.CardUsageLimitations;
import org.project.karto.domain.common.value_objects.Email;
import org.project.karto.domain.common.value_objects.KeyAndCounter;
import org.project.karto.domain.common.value_objects.Phone;
import org.project.karto.domain.companies.entities.Company;
import org.project.karto.domain.companies.enumerations.CompanyStatus;
import org.project.karto.domain.companies.repository.CompanyRepository;
import org.project.karto.domain.companies.value_objects.CompanyName;
import org.project.karto.domain.companies.value_objects.RegistrationNumber;
import org.project.karto.domain.user.values_objects.Password;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static com.hadzhy.jetquerious.sql.QueryForge.*;

@ApplicationScoped
public class JDBCCompanyRepository implements CompanyRepository {

    private final JetQuerious jet;

    static final String SAVE_COMPANY = insert()
            .into("companies")
            .column("id")
            .column("state_code")
            .column("registration_number")
            .column("company_name")
            .column("email")
            .column("phone")
            .column("password")
            .column("secret_key")
            .column("counter")
            .column("status")
            .column("expiration_period_days")
            .column("max_usage_count")
            .column("creation_date")
            .column("last_updated")
            .values()
            .build()
            .sql();

    static final String UPDATE_COMPANY = update("companies")
            .set("""
                  expiration_period_days = ?,
                  max_usage_count = ?,
                  last_updated = ?
                  """)
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_PASSWORD = update("companies")
            .set("password = ?, last_updated = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_COUNTER = update("companies")
            .set("counter = ?, last_updated = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_VERIFICATION = update("companies")
            .set("status = ?, last_updated = ?")
            .where("id = ?")
            .build()
            .sql();

    static final String FIND_BY_ID = select()
            .all()
            .from("companies")
            .where("id = ?")
            .build()
            .sql();

    static final String FIND_BY_REGISTRATION_NUMBER = select()
            .all()
            .from("companies")
            .where("registration_number = ?")
            .build()
            .sql();

    static final String FIND_BY_EMAIL = select()
            .all()
            .from("companies")
            .where("email = ?")
            .build()
            .sql();

    static final String FIND_BY_PHONE = select()
            .all()
            .from("companies")
            .where("phone = ?")
            .build()
            .sql();

    static final String FIND_BY_COMPANY_NAME = select()
            .all()
            .from("companies")
            .where("company_name = ?")
            .build()
            .sql();

    static final String IS_REGISTRATION_NUMBER_EXISTS = select()
            .count("registration_number")
            .from("companies")
            .where("registration_number = ?")
            .build()
            .sql();

    static final String IS_PHONE_EXISTS = select()
            .count("phone")
            .from("companies")
            .where("phone = ?")
            .build()
            .sql();

    static final String IS_EMAIL_EXISTS = select()
            .count("email")
            .from("companies")
            .where("email = ?")
            .build()
            .sql();

    static final String IS_COMPANY_NAME_EXISTS = select()
            .count("company_name")
            .from("companies")
            .where("company_name = ?")
            .build()
            .sql();

    JDBCCompanyRepository() {
        jet = JetQuerious.instance();
    }

    @Override
    public Result<Integer, Throwable> save(Company company) {
        return jet.write(SAVE_COMPANY, company.id(), company.registrationNumber().countryCode(), company.registrationNumber().value(),
                        company.companyName(), company.email(), company.phone(), company.password(), company.keyAndCounter().key(),
                        company.keyAndCounter().counter(), company.companyStatus(), company.cardUsageLimitation().expirationDays(),
                        company.cardUsageLimitation().maxUsageCount(), company.creationDate(), company.lastUpdated());
    }

    @Override
    public Result<Integer, Throwable> updateCardUsageLimitations(Company company) {
        CardUsageLimitations cardUsageLimitations = company.cardUsageLimitation();

        return jet.write(UPDATE_COMPANY, cardUsageLimitations.expirationDays(),
                        cardUsageLimitations.maxUsageCount(), company.lastUpdated(), company.id());
    }

    @Override
    public Result<Integer, Throwable> updatePassword(Company company) {
        return jet.write(UPDATE_PASSWORD, company.password(), company.lastUpdated(), company.id());
    }

    @Override
    public Result<Integer, Throwable> updateCounter(Company company) {
        return jet.write(UPDATE_COUNTER, company.keyAndCounter().counter(), company.lastUpdated(), company.id());
    }

    @Override
    public Result<Integer, Throwable> updateVerification(Company company) {
        return jet.write(UPDATE_VERIFICATION, company.companyStatus(), company.lastUpdated(), company.id());
    }

    @Override
    public Result<Company, Throwable> findBy(UUID companyID) {
        var read = jet.read(FIND_BY_ID, this::companyMapper, companyID);
        return mapResult(read);
    }

    @Override
    public Result<Company, Throwable> findBy(RegistrationNumber registrationNumber) {
        var read = jet.read(FIND_BY_REGISTRATION_NUMBER, this::companyMapper, registrationNumber.value());
        return mapResult(read);
    }

    @Override
    public Result<Company, Throwable> findBy(Phone phone) {
        var read = jet.read(FIND_BY_PHONE, this::companyMapper, phone);
        return mapResult(read);
    }

    @Override
    public Result<Company, Throwable> findBy(Email email) {
        var read = jet.read(FIND_BY_EMAIL, this::companyMapper, email);
        return mapResult(read);
    }

    @Override
    public Result<Company, Throwable> findBy(CompanyName companyName) {
        var read = jet.read(FIND_BY_COMPANY_NAME, this::companyMapper, companyName);
        return mapResult(read);
    }

    @Override
    public boolean isExists(RegistrationNumber registrationNumber) {
        return jet.readObjectOf(IS_REGISTRATION_NUMBER_EXISTS, Integer.class, registrationNumber.value())
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking registration number existence.");
                    return false;
                });
    }

    @Override
    public boolean isExists(Phone phone) {
        return jet.readObjectOf(IS_PHONE_EXISTS, Integer.class, phone)
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking phone existence.");
                    return false;
                });
    }

    @Override
    public boolean isExists(Email email) {
        return jet.readObjectOf(IS_EMAIL_EXISTS, Integer.class, email)
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking email existence.");
                    return false;
                });
    }

    @Override
    public boolean isExists(CompanyName companyName) {
        return jet.readObjectOf(IS_COMPANY_NAME_EXISTS, Integer.class, companyName)
                .mapSuccess(count -> count != null && count > 0)
                .orElseGet(() -> {
                    Log.error("Error checking company name existence.");
                    return false;
                });
    }

    private Company companyMapper(ResultSet rs) throws SQLException {
        return Company.fromRepository(
                UUID.fromString(rs.getString("id")),
                new RegistrationNumber(rs.getString("state_code"), rs.getString("registration_number")),
                new CompanyName(rs.getString("company_name")),
                new Email(rs.getString("email")),
                new Phone(rs.getString("phone")),
                rs.getTimestamp("creation_date").toLocalDateTime(),
                rs.getTimestamp("last_updated").toLocalDateTime(),
                new Password(rs.getString("password")),
                new KeyAndCounter(rs.getString("secret_key"), rs.getInt("counter")),
                CompanyStatus.valueOf(rs.getString("status")),
                CardUsageLimitations.of(rs.getInt("expiration_period_days"), rs.getInt("max_usage_count"))
        );
    }

    private static Result<Company, Throwable> mapResult(com.hadzhy.jetquerious.util.Result<Company, Throwable> read) {
        return new Result<>(read.value(), read.throwable(), read.success());
    }
}
