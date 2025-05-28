CREATE TABLE gift_card_otp (
    otp VARCHAR NOT NULL,
    card_id CHAR(36) NOT NULL,
    is_confirmed BOOLEAN NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    expiration_date TIMESTAMP NOT NULL,
    PRIMARY KEY (otp),
    CONSTRAINT card_otp_fk FOREIGN KEY (card_id) REFERENCES gift_card(id)
);

CREATE FUNCTION delete_confirmed_gift_card_otp() RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM gift_card_otp
    WHERE is_confirmed = true
      AND card_id = NEW.card_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_delete_verified_gift_card_otp
AFTER UPDATE ON gift_card_otp
FOR EACH ROW
EXECUTE FUNCTION delete_confirmed_gift_card_otp();