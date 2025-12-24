CREATE TABLE bill
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    billing_date datetime              NULL,
    customer_id  BIGINT                NOT NULL,
    CONSTRAINT pk_bill PRIMARY KEY (id)
);