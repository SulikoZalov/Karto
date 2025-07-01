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
    PRIMARY KEY (id)
);