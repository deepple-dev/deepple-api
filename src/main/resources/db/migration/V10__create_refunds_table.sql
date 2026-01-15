-- Create refunds table for tracking payment refunds
CREATE TABLE refunds
(
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    order_id          BIGINT       NOT NULL,
    member_id         BIGINT       NOT NULL,
    transaction_id    VARCHAR(255) NOT NULL,
    product_id        VARCHAR(255) NOT NULL,
    quantity          INT          NOT NULL,
    refund_amount     BIGINT       NOT NULL,
    payment_method    VARCHAR(50)  NOT NULL,
    notification_type VARCHAR(50)  NOT NULL,
    refunded_at       DATETIME(6)  NOT NULL,
    created_at        DATETIME(6)  NOT NULL,
    updated_at        DATETIME(6),
    PRIMARY KEY (id),
    INDEX             idx_refunds_member_id (member_id),
    CONSTRAINT uk_refunds_transaction_id UNIQUE (transaction_id),
    CONSTRAINT fk_refunds_order FOREIGN KEY (order_id) REFERENCES orders (id)
) ENGINE = InnoDB;
