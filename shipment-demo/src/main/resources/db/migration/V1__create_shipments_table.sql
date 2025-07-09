create table shipments
(
    id              bigserial primary key,
    shipment_id     varchar(255) not null unique,
    order_id        varchar(255) not null,
    customer_id     varchar(255) not null,
    status          varchar(50)  not null,
    tracking_number varchar(255),
    carrier_name    varchar(255),
    created_at      timestamp    not null default current_timestamp,
    updated_at      timestamp    not null default current_timestamp
);

create index idx_shipments_order_id on shipments (order_id);
