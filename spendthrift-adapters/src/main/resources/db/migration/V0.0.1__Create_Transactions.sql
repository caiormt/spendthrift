CREATE TABLE IF NOT EXISTS TRANSACTIONS
(
    id          UUID                        NOT NULL,
    datetime    TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    amount      DECIMAL(13, 4)              NOT NULL,
    currency    CHAR(3)                     NOT NULL,
    description VARCHAR(255)                NOT NULL,
    PRIMARY KEY (id)
);
