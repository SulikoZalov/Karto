CREATE TABLE gift_card (
    id CHAR(36) NOT NULL,
    buyer_id CHAR(36) NOT NULL,
    owner_id CHAR(36),
    store_id CHAR(36),
    gift_card_status VARCHAR(7) NOT NULL CHECK (gift_card_status IN ('PENDING', 'ACTIVE', 'EXPIRED', 'USED_UP'))
    balance BIGINT NOT NULL,
    count_of_uses SMALLINT NOT NULL,
    is_verified BOOLEAN NOT NULL,
    secret_key VARCHAR NOT NULL,
    counter BIGINT NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    expiration_date TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);