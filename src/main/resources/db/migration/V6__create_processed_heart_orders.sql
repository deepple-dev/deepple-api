create table processed_heart_orders
(
    id             bigint      not null auto_increment,
    operation_type varchar(50) not null,
    order_id       bigint      not null,
    created_at     datetime(6) not null,
    updated_at     datetime(6),
    primary key (id),
    constraint uk_processed_heart_orders_operation_type_order_id unique (operation_type, order_id)
) engine=InnoDB;
