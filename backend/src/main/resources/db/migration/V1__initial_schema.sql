create table tenants (
    id uuid primary key,
    name varchar(160) not null,
    slug varchar(80) not null unique,
    created_at timestamptz not null default now()
);

create table products (
    id uuid primary key,
    tenant_id uuid not null references tenants(id),
    name varchar(180) not null,
    price numeric(12, 2) not null check (price >= 0),
    stock_quantity integer not null default 0 check (stock_quantity >= 0),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index idx_products_tenant_name on products (tenant_id, name);

alter table products enable row level security;

create policy products_tenant_isolation
    on products
    using (tenant_id::text = current_setting('app.current_tenant_id', true))
    with check (tenant_id::text = current_setting('app.current_tenant_id', true));
