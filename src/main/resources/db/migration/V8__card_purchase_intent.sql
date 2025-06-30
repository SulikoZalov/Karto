CREATE TABLE card_purchase_intent (
    id CHAR(36) NOT NULL,
    buyer_id CHAR(36) NOT NULL,
    order_id BIGINT NOT NULL,
    total_payed_amount NUMERIC NOT NULL,
    creation_date TIMESTAMP NOT NULL,
    result_date TIMESTAMP,
    status VARCHAR(7) NOT NULL CHECK (status IN ('PENDING', 'SUCCESS', 'CANCEL', 'FAILURE')),
    removed_fee NUMERIC,
    PRIMARY KEY (id)
);

CREATE SEQUENCE card_purchase_intent_order_id_seq;