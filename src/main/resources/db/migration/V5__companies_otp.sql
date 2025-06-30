CREATE TABLE companies_otp (
    otp CHAR(6) NOT NULL,
    company_id CHAR(36) NOT NULL,
    is_confirmed BOOLEAN NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    expiration_date TIMESTAMP NOT NULL,
    PRIMARY KEY (otp),
    CONSTRAINT company_otp_fk FOREIGN KEY (company_id) REFERENCES companies(id)
);

CREATE UNIQUE INDEX unique_active_otp_per_company
ON companies_otp(company_id)
WHERE is_confirmed = false;

CREATE FUNCTION delete_confirmed_company_otp() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_confirmed THEN
        DELETE FROM companies_otp
        WHERE is_confirmed = true
          AND company_id = NEW.company_id
          AND otp <> NEW.otp;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_delete_verified_company_otp
AFTER UPDATE ON companies_otp
FOR EACH ROW
EXECUTE FUNCTION delete_confirmed_company_otp();