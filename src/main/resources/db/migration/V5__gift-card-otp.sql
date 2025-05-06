CREATE TABLE gift_card_otp (
    otp VARCHAR NOT NULL,
    card_id CHAR(36) NOT NULL,
    is_confirmed BOOLEAN NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    expiration_date TIMESTAMP NOT NULL,
    PRIMARY KEY (otp),
    CONSTRAINT card_otp_fk FOREIGN KEY (card_id) REFERENCES gift_card(id)
);