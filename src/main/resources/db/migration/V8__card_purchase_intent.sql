CREATE TABLE card_purchase_intent (
    id CHAR(36) NOT NULL,
    buyer_id CHAR(36) NOT NULL,
    store_id CHAR(36),
    order_id BIGINT NOT NULL,
    total_payed_amount NUMERIC NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    result_date TIMESTAMP,
    status VARCHAR(7) NOT NULL CHECK (status IN ('PENDING', 'SUCCESS', 'CANCEL', 'FAILURE')),
    removed_fee NUMERIC,
    PRIMARY KEY (id),
    CONSTRAINT fk_purchase_buyer_account FOREIGN KEY (buyer_id) REFERENCES user_account(id) ON DELETE CASCADE,
    CONSTRAINT fk_purchase_store_account FOREIGN KEY (store_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE SEQUENCE card_purchase_intent_order_id_seq;

CREATE INDEX purchase_buyer_index ON card_purchase_intent (buyer_id);

CREATE INDEX purchase_owner_index ON card_purchase_intent (store_id);

CREATE UNIQUE INDEX purchase_order_index ON card_purchase_intent (order_id);