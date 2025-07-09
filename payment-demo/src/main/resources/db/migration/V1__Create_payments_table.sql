-- create payments table
create table payments
(
    id                 bigserial primary key,
    payment_id         varchar(255)   not null unique,
    order_id           varchar(255)   not null,
    customer_id        varchar(255)   not null,
    amount             decimal(10, 2) not null,
    status             varchar(50)    not null,
    authorization_code varchar(255),
    transaction_id     varchar(255),
    payment_method     varchar(50),
    failure_reason     varchar(500),
    created_at         timestamp      not null default current_timestamp,
    updated_at         timestamp      not null default current_timestamp
);

create index idx_payments_order_id on payments (order_id);
create index idx_payments_payment_id on payments (payment_id);
create index idx_payments_customer_id on payments (customer_id);
create index idx_payments_status on payments (status);

