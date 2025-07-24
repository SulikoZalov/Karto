CREATE TABLE chck (
    id CHAR(36) NOT NULL,
    order_id BIGINT NOT NULL,
    buyer_id CHAR(36) NOT NULL,
    store_id CHAR(36),
    card_id CHAR(36),
    total_amount NUMERIC NOT NULL,
    currency CHAR(3) NOT NULL,
    payment_type VARCHAR(13) NOT NULL CHECK ( payment_type in ('GOOGLE_PAY', 'APPLE_PAY', 'NATIVE_BANK', 'FOREIGN_BANK', 'KARTO_PAYMENT')),
    internal_fee NUMERIC NOT NULL,
    external_fee NUMERIC NOT NULL,
    payment_system VARCHAR(48) NOT NULL,
    description VARCHAR(255) NOT NULL,
    bank_name VARCHAR(255) NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_chck_buyer_account FOREIGN KEY (buyer_id) REFERENCES user_account(id) ON DELETE CASCADE,
    CONSTRAINT fk_chck_store_account FOREIGN KEY (store_id) REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT fk_chck_card_account FOREIGN KEY (card_id) REFERENCES gift_card(id) ON DELETE CASCADE
);

CREATE INDEX chck_buyer_index ON chck (buyer_id);

CREATE INDEX chck_card_index ON chck (card_id);

CREATE INDEX chck_store_index ON chck (store_id);

CREATE UNIQUE INDEX chck_order_index ON chck (order_id);