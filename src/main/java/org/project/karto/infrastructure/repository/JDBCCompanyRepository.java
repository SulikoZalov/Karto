package org.project.karto.infrastructure.repository;

import com.hadzhy.jetquerious.jdbc.JetQuerious;
import com.hadzhy.jetquerious.sql.QueryForge;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.project.karto.domain.common.containers.Result;
import org.project.karto.domain.common.value_objects.CardUsageLimitations;
import org.project.karto.domain.common.value_objects.Email;
import org.project.karto.domain.common.value_objects.Phone;
import org.project.karto.domain.companies.entities.Company;
import org.project.karto.domain.companies.repository.CompanyRepository;
import org.project.karto.domain.companies.value_objects.CompanyName;
import org.project.karto.domain.companies.value_objects.RegistrationNumber;
import org.project.karto.domain.user.values_objects.Password;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Period;
import java.util.UUID;

import static com.hadzhy.jetquerious.sql.QueryForge.insert;
import static com.hadzhy.jetquerious.sql.QueryForge.select;

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
            .column("expiration_period_days")
            .column("max_usage_count")
            .column("creation_date")
            .column("last_updated")
            .values()
            .build()
            .sql();

    static final String UPDATE_COMPANY = QueryForge.update("companies")
            .set("""
                  expiration_period_days = ?,
                  max_usage_count = ?,
                  last_updated = ?
                  """)
            .where("id = ?")
            .build()
            .sql();

    static final String UPDATE_PASSWORD = QueryForge.update("companies")
            .set("password = ?, last_updated = ?")
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

    JDBCCompanyRepository() {
        jet = JetQuerious.instance();
    }

    @Override
    public void save(Company company) {
        jet.write(SAVE_COMPANY, company.id(), company.registrationNumber().countryCode(), company.registrationNumber().value(),
                        company.companyName(), company.email(), company.phone(), company.password(),
                        company.cardUsagesLimitation().expirationDays(), company.cardUsagesLimitation().maxUsageCount(),
                        company.creationDate(), company.lastUpdated())
                .ifFailure(throwable -> Log.errorf("Error saving company aggregate: %s.", throwable.getMessage()));
    }

    @Override
    public void update(Company company) {
        CardUsageLimitations cardUsageLimitations = company.cardUsagesLimitation();

        jet.write(UPDATE_COMPANY, cardUsageLimitations.expirationDays(),
                        cardUsageLimitations.maxUsageCount(), company.lastUpdated(), company.id())
                .ifFailure(throwable -> Log.errorf("Error update company aggregate: %s.", throwable.getMessage()));
    }

    @Override
    public void updatePassword(Company company) {
        jet.write(UPDATE_PASSWORD, company.password(), company.id())
                .ifFailure(throwable -> Log.errorf("Error update password: %s.", throwable.getMessage()));
    }

    @Override
    public Result<Company, Throwable> findBy(UUID companyID) {
        var read = jet.read(FIND_BY_ID, this::companyMapper, companyID);
        return new Result<>(read.value(), read.throwable(), read.success());
    }

    @Override
    public Result<Company, Throwable> findBy(RegistrationNumber registrationNumber) {
        var read = jet.read(FIND_BY_REGISTRATION_NUMBER, this::companyMapper, registrationNumber.value());
        return new Result<>(read.value(), read.throwable(), read.success());
    }

    @Override
    public Result<Company, Throwable> findBy(Phone phone) {
        var read = jet.read(FIND_BY_PHONE, this::companyMapper, phone);
        return new Result<>(read.value(), read.throwable(), read.success());
    }

    @Override
    public Result<Company, Throwable> findBy(Email email) {
        var read = jet.read(FIND_BY_EMAIL, this::companyMapper, email);
        return new Result<>(read.value(), read.throwable(), read.success());
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
                new CardUsageLimitations(Period.ofDays(rs.getInt("expiration_period_days")), rs.getInt("max_usage_count"))
        );
    }
}
