drop table if exists "t_orders";
create table "t_orders"
(
    "id"          uuid primary key default gen_random_uuid(),
    "building"    int              not null,
    "room"        int              not null,
    "status"      text             not null,
    "ingredients" uuid[]           default array[]::uuid[],
    "created_at"  timestamp        default now(),
    "updated_at"  timestamp        default now()
);