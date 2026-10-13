insert into tenants (id, name, slug)
values ('00000000-0000-0000-0000-000000000001', 'Local Demo Tenant', 'local-demo')
on conflict (id) do nothing;
