create schema if not exists test;

create table if not exists test.Customer (
    cu_id NUMBER not null,
    name Varchar(255) not null,
    country Varchar(3) null,
    updated_by Varchar(255) null,
    updated_at timestamp null
);

create table if not exists test.Product (
    pr_id NUMBER not null,
    name Varchar(255) not null,
    description Varchar(255) not null,
    updated_by Varchar(255) null,
    updated_at timestamp null
);

create table if not exists test.Product_Xref (
    pr_id NUMBER not null,
    xref_type Varchar(255) not null,
    xref_value Varchar(255) not null,
    updated_by Varchar(255) null,
    updated_at timestamp null
);

create table if not exists test.Orders (
    or_id NUMBER not null,
    pr_id NUMBER not null,
    cu_id NUMBER not null,
    amount NUMBER not null,
    delivery_type Varchar(255) null,
    updated_by Varchar(255) null,
    updated_at timestamp null
);
