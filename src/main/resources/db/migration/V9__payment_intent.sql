CREATE TABLE payment_intent (
    id CHAR(36) NOT NULL,
    buyer_id CHAR(36) NOT NULL,
    card_id CHAR(36) NOT NULL,
    store_id CHAR(36),
    order_id BIGINT NOT NULL,
    total_amount NUMERIC NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    result_date TIMESTAMP,
    status VARCHAR(7) NOT NULL CHECK (status IN ('PENDING', 'SUCCESS', 'CANCEL', 'FAILURE')),
    is_confirmed BOOLEAN NOT NULL,
    description TEXT,
    fee NUMERIC NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_payment_buyer_account FOREIGN KEY (buyer_id) REFERENCES user_account(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_store_account FOREIGN KEY (store_id) REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT fk_payment_card_account FOREIGN KEY (card_id) REFERENCES gift_card(id) ON DELETE CASCADE
);

CREATE INDEX payment_buyer_index ON payment_intent (buyer_id);

CREATE INDEX payment_card_index ON payment_intent (card_id);

CREATE INDEX payment_store_index ON payment_intent (store_id);

CREATE UNIQUE INDEX payment_order_index ON payment_intent (order_id);