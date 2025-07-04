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
    payment_system VARCHAR(48),
    description TEXT,
    creation_date TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
)