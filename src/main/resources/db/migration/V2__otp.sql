CREATE TABLE otp (
    otp VARCHAR NOT NULL,
    user_id CHAR(36) NOT NULL,
    is_confirmed BOOLEAN NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    expiration_date TIMESTAMP NOT NULL,
    PRIMARY KEY (otp),
    CONSTRAINT user_otp_fk FOREIGN KEY (user_id) REFERENCES user_account(id)
);

CREATE FUNCTION delete_confirmed_otp() RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM otp
    WHERE is_confirmed = true
      AND user_id = NEW.user_id
      AND otp <> NEW.otp;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_delete_verified_otp
AFTER INSERT OR UPDATE ON otp
FOR EACH STATEMENT
EXECUTE FUNCTION delete_confirmed_otp();