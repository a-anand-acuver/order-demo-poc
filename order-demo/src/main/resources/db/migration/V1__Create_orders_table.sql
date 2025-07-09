-- create orders table
create table orders
(
    id             bigserial primary key,
    order_id       varchar(255)   not null unique,
    customer_id    varchar(255)   not null,
    product_id     varchar(255)   not null,
    quantity       integer        not null,
    price          decimal(10, 2) not null,
    total_amount   decimal(10, 2) not null,
    status         varchar(50)    not null,
    payment_status varchar(50)    not null,
    created_at     timestamp      not null default current_timestamp,
    updated_at     timestamp      not null default current_timestamp
);

create index idx_orders_order_id on orders (order_id);
create index idx_orders_customer_id on orders (customer_id);
create index idx_orders_status on orders (status);
