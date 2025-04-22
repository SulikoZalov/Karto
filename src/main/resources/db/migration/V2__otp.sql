CREATE TABLE otp (
    otp VARCHAR NOT NULL,
    user_id CHAR(36) NOT NULL,
    is_confirmed BOOLEAN NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    expiration_date TIMESTAMP NOT NULL,
    PRIMARY KEY (otp),
    CONSTRAINT user_otp_fk FOREIGN KEY (user_id) REFERENCES user_account(id)
);

CREATE FUNCTION delete_unused_otp() RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM otp
    WHERE is_confirmed = true
       OR (is_confirmed = false AND expiration_date <= NOW());
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_delete_unused_otp
AFTER INSERT OR UPDATE ON otp
FOR EACH STATEMENT
EXECUTE FUNCTION delete_unused_otp();