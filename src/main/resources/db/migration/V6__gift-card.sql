CREATE TABLE gift_card (
    id CHAR(36) NOT NULL,
    buyer_id CHAR(36) NOT NULL,
    owner_id CHAR(36),
    store_id CHAR(36),
    gift_card_status VARCHAR(7) NOT NULL CHECK (gift_card_status IN ('PENDING', 'ACTIVE', 'EXPIRED', 'USED_UP')),
    balance NUMERIC NOT NULL,
    count_of_uses SMALLINT NOT NULL,
    max_count_of_uses SMALLINT NOT NULL,
    secret_key VARCHAR NOT NULL,
    counter BIGINT NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    expiration_date TIMESTAMP NOT NULL,
    last_usage TIMESTAMP NOT NULL,
    version BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_buyer_account FOREIGN KEY (buyer_id) REFERENCES user_account(id) ON DELETE CASCADE,
    CONSTRAINT fk_owner_account FOREIGN KEY (owner_id) REFERENCES user_account(id) ON DELETE CASCADE,
    CONSTRAINT fk_store_account FOREIGN KEY (store_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE INDEX gift_card_buyer_index ON gift_card (buyer_id);

CREATE INDEX gift_card_owner_index ON gift_card (owner_id);